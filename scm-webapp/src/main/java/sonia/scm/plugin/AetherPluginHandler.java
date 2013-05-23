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

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.boot.BootstrapListener;
import sonia.scm.boot.Classpath;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

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

  /** the logger for AetherPluginHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(AetherPluginHandler.class);

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
    SCMContextProvider context, ScmConfiguration configuration)
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
      throw new ConfigurationException(
        "could not create jaxb context for classpath file", ex);
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
        logger.error("could not read classpath file", ex);
      }
    }

    IOUtil.mkdirs(localRepositoryDirectory);
    repositorySystem = Aether.createRepositorySystem();
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

    Dependency dependency = Aether.createDependency(gav);
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
    remoteRepositories = Lists.newArrayList();

    for (PluginRepository repository : repositories)
    {
      remoteRepositories.add(Aether.createRemoteRepository(configuration,
        repository));
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param dependency
   * @param dependencies
   * @param localDependencies
   */
  private void collectDependencies(Dependency dependency,
    List<Dependency> localDependencies)
  {
    try
    {
      AetherDependencyResolver resolver =
        new AetherDependencyResolver(configuration, repositorySystem,
          localRepository, remoteRepositories);

      resolver.resolveRemoteDependency(dependency);

      for (Dependency localDependency : localDependencies)
      {
        resolver.resolveLocalDependency(localDependency);
      }

      if (classpath == null)
      {
        classpath = new Classpath();
      }

      synchronized (Classpath.class)
      {

        Set<String> classpathSet =
          createClasspathSet(resolver.createClassPath());

        classpath.setPathSet(classpathSet);
        storeClasspath();
      }
    }
    catch (Exception ex)
    {
      throw new PluginException(
        "could not collect dependencies or store classpath file", ex);
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
    List<Dependency> dependencies = Lists.newArrayList();
    Collection<PluginInformation> installed =
      pluginManager.get(new StatePluginFilter(PluginState.INSTALLED));

    if (installed != null)
    {
      for (PluginInformation plugin : installed)
      {
        String id = plugin.getId();

        if (Util.isNotEmpty(id) && ((skipId == null) ||!id.equals(skipId)))
        {
          dependencies.add(Aether.createDependency(id));
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
