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
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.boot.BootstrapListener;
import sonia.scm.boot.Classpath;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
   * @param context
   */
  public AetherPluginHandler(SCMContextProvider context)
  {
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
    CollectRequest request = new CollectRequest(dependency, remoteRepositories);
    MavenRepositorySystemSession session = new MavenRepositorySystemSession();

    session.setLocalRepositoryManager(
        repositorySystem.newLocalRepositoryManager(localRepository));

    try
    {

      /*
       * DependencyNode node = repositorySystem.collectDependencies(session,
       *                       request).getRoot();
       */
      List<ArtifactResult> artifacts =
        repositorySystem.resolveDependencies(session, request, FILTER);

      synchronized (Classpath.class)
      {
        if (classpath == null)
        {
          classpath = new Classpath();
        }

        for (ArtifactResult result : artifacts)
        {
          File file = result.getArtifact().getFile();

          if (logger.isDebugEnabled())
          {
            logger.debug("added {} to classpath", file.getPath());
          }

          classpath.add(
              file.getAbsolutePath().substring(
                localRepositoryDirectory.getAbsolutePath().length()));
        }

        Marshaller marshaller = jaxbContext.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(classpath, classpathFile);
      }
    }
    catch (Exception ex)
    {
      throw new PluginLoadException(ex);
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
      remoteRepositories.add(new RemoteRepository(repository.getId(),
              "default", repository.getUrl()));
    }
  }

  //~--- methods --------------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Classpath classpath;

  /** Field description */
  private File classpathFile;

  /** Field description */
  private JAXBContext jaxbContext;

  /** Field description */
  private LocalRepository localRepository;

  /** Field description */
  private File localRepositoryDirectory;

  /** Field description */
  private List<RemoteRepository> remoteRepositories;

  /** Field description */
  private RepositorySystem repositorySystem;
}
