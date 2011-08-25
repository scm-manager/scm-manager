/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.boot.BootstrapListener;
import sonia.scm.boot.Classpath;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Sebastian Sdorra
 */
public class AetherPluginHandler
{

  /** Field description */
  public static final String PLUGIN_SCOPE = "runtime";

  /** the logger for AetherPluginHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(AetherPluginHandler.class);

  /** Field description */
  private static final DependencyFilter FILTER = new AetherDependencyFilter();

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param pluginManager
   * @param context
   * @param configuration
   */
  public AetherPluginHandler(PluginManager pluginManager,
                             SCMContextProvider context,
                             ScmConfiguration configuration)
  {
    this.pluginManager = pluginManager;
    this.configuration = configuration;
    localRepositoryDirectory = new File(context.getBaseDirectory(),
            BootstrapListener.PLUGIN_DIRECTORY);

    try
    {
      jaxbContext = JAXBContext.newInstance(Classpath.class);
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException(ex);
    }

    classpathFile = new File(localRepositoryDirectory,
                             BootstrapListener.PLUGIN_CLASSPATHFILE);

    if (classpathFile.exists())
    {
      try
      {
        classpath =
          (Classpath) jaxbContext.createUnmarshaller().unmarshal(classpathFile);
      }
      catch (JAXBException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    IOUtil.mkdirs(localRepositoryDirectory);
    repositorySystem = createRepositorySystem();
    localRepository = new LocalRepository(localRepositoryDirectory);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param gav
   */
  public void install(String gav)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("try to install plugin with gav: {}", gav);
    }

    Dependency dependency = new Dependency(new DefaultArtifact(gav),
                              PLUGIN_SCOPE);
    List<Dependency> dependencies = getInstalledDependencies(null);

    collectDependencies(dependency, dependencies);
  }

  /**
   * TODO: remove dependencies and remove files
   *
   *
   *
   * @param id
   */
  public void uninstall(String id)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("try to uninstall plugin: {}", id);
    }

    if (classpath != null)
    {
      synchronized (Classpath.class)
      {
        List<Dependency> dependencies = getInstalledDependencies(id);

        collectDependencies(null, dependencies);
      }
    }
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositories
   */
  public void setPluginRepositories(Collection<PluginRepository> repositories)
  {
    remoteRepositories = new ArrayList<RemoteRepository>();

    for (PluginRepository repository : repositories)
    {
      RemoteRepository rr = new RemoteRepository(repository.getId(), "default",
                              repository.getUrl());

      if (configuration.isEnableProxy())
      {
        Proxy proxy = createProxy();

        if (logger.isDebugEnabled())
        {
          logger.debug("enable proxy {} for {}", repository.getUrl(),
                       proxy.getHost());
        }

        rr.setProxy(proxy);
      }

      remoteRepositories.add(rr);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param dependency
   * @param dependencies
   */
  private void collectDependencies(Dependency dependency,
                                   List<Dependency> dependencies)
  {
    CollectRequest request = new CollectRequest(dependency, dependencies,
                               remoteRepositories);
    MavenRepositorySystemSession session = new MavenRepositorySystemSession();

    session.setLocalRepositoryManager(
        repositorySystem.newLocalRepositoryManager(localRepository));

    try
    {
      DependencyNode node = repositorySystem.collectDependencies(session,
                              request).getRoot();
      DependencyRequest dr = new DependencyRequest(node, FILTER);

      repositorySystem.resolveDependencies(session, dr);

      synchronized (Classpath.class)
      {
        if (classpath == null)
        {
          classpath = new Classpath();
        }

        PreorderNodeListGenerator nodeListGenerator =
          new PreorderNodeListGenerator();

        node.accept(nodeListGenerator);

        Set<String> classpathSet =
          createClasspathSet(nodeListGenerator.getClassPath());

        classpath.setPathSet(classpathSet);
        storeClasspath();
      }
    }
    catch (Exception ex)
    {
      throw new PluginException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param classpathString
   *
   * @return
   */
  private Set<String> createClasspathSet(String classpathString)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("set new plugin classpath: {}", classpathString);
    }

    Set<String> classpathSet = new LinkedHashSet<String>();

    if (Util.isNotEmpty(classpathString))
    {
      String[] classpathParts = classpathString.split(File.pathSeparator);
      int prefixLength = localRepositoryDirectory.getAbsolutePath().length();

      for (String classpathPart : classpathParts)
      {
        classpathSet.add(classpathPart.substring(prefixLength));
      }
    }

    return classpathSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Proxy createProxy()
  {
    return new Proxy(Proxy.TYPE_HTTP, configuration.getProxyServer(),
                     configuration.getProxyPort(), null);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private RepositorySystem createRepositorySystem()
  {
    DefaultServiceLocator locator = new DefaultServiceLocator();

    locator.addService(VersionResolver.class, DefaultVersionResolver.class);
    locator.addService(VersionRangeResolver.class,
                       DefaultVersionRangeResolver.class);
    locator.addService(ArtifactDescriptorReader.class,
                       DefaultArtifactDescriptorReader.class);
    locator.setServices(WagonProvider.class, new WagonProvider()
    {
      @Override
      public Wagon lookup(String roleHint) throws Exception
      {
        return new LightweightHttpWagon();
      }
      @Override
      public void release(Wagon wagon) {}
    });
    locator.addService(RepositoryConnectorFactory.class,
                       WagonRepositoryConnectorFactory.class);

    return locator.getService(RepositorySystem.class);
  }

  /**
   * Method description
   *
   *
   * @throws JAXBException
   */
  private void storeClasspath() throws JAXBException
  {
    Marshaller marshaller = jaxbContext.createMarshaller();

    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(classpath, classpathFile);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param skipId
   * @return
   */
  private List<Dependency> getInstalledDependencies(String skipId)
  {
    List<Dependency> dependencies = new ArrayList<Dependency>();
    Collection<PluginInformation> installed =
      pluginManager.get(new StatePluginFilter(PluginState.INSTALLED));

    if (installed != null)
    {
      for (PluginInformation plugin : installed)
      {
        String id = plugin.getId();

        if (Util.isNotEmpty(id) && ((skipId == null) ||!id.equals(skipId)))
        {
          dependencies.add(new Dependency(new DefaultArtifact(id),
                                          PLUGIN_SCOPE));
        }
      }
    }

    return dependencies;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Classpath classpath;

  /** Field description */
  private File classpathFile;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private JAXBContext jaxbContext;

  /** Field description */
  private LocalRepository localRepository;

  /** Field description */
  private File localRepositoryDirectory;

  /** Field description */
  private PluginManager pluginManager;

  /** Field description */
  private List<RemoteRepository> remoteRepositories;

  /** Field description */
  private RepositorySystem repositorySystem;
}
