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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSHooks;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.util.SVNDebugLog;
import sonia.scm.ContextEntry;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.logging.SVNKitLogger;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.repository.spi.SvnRepositoryServiceProvider;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.Util;

import java.io.File;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Extension
public class SvnRepositoryHandler
  extends AbstractSimpleRepositoryHandler<SvnConfig>
{

  public static final String PROPERTY_UUID = "svn.uuid";

  public static final String RESOURCE_VERSION = "sonia/scm/version/scm-svn-plugin";

  public static final String TYPE_DISPLAYNAME = "Subversion";

  public static final String TYPE_NAME = "svn";

  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME,
                                    TYPE_DISPLAYNAME,
                                    SvnRepositoryServiceProvider.COMMANDS);
  private static final String CONFIG_FILE_NAME = "scm-manager.conf";
  private static final String CONFIG_SECTION_SCMM = "scmm";
  private static final String CONFIG_KEY_REPOSITORY_ID = "repositoryid";

  private static final Logger logger =
    LoggerFactory.getLogger(SvnRepositoryHandler.class);

  @Inject
  public SvnRepositoryHandler(ConfigurationStoreFactory storeFactory,
                              HookEventFacade eventFacade,
                              RepositoryLocationResolver repositoryLocationResolver,
                              PluginLoader pluginLoader)
  {
    super(storeFactory, repositoryLocationResolver, pluginLoader);

    // register logger
    SVNDebugLog.setDefaultLog(new SVNKitLogger());

    // setup FSRepositoryFactory for SvnRepositoryBrowser
    FSRepositoryFactory.setup();

    // register hook
    if (eventFacade != null)
    {
      FSHooks.registerHook(new SvnRepositoryHook(eventFacade, this));
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn(
        "unable to register hook, beacause of missing repositorymanager");
    }
  }

  @Override
  public ImportHandler getImportHandler()
  {
    return new SvnImportHandler(this);
  }

  @Override
  public RepositoryType getType()
  {
    return TYPE;
  }

  @Override
  public String getVersionInformation()
  {
    return getStringFromResource(RESOURCE_VERSION, DEFAULT_VERSION_INFORMATION);
  }

  @Override
  protected void create(Repository repository, File directory) throws InternalRepositoryException {
    Compatibility comp = config.getCompatibility();

    if (logger.isDebugEnabled())
    {
      StringBuilder log = new StringBuilder("create svn repository \"");

      log.append(directory.getName()).append("\": pre14Compatible=");
      log.append(comp.isPre14Compatible()).append(", pre15Compatible=");
      log.append(comp.isPre15Compatible()).append(", pre16Compatible=");
      log.append(comp.isPre16Compatible()).append(", pre17Compatible=");
      log.append(comp.isPre17Compatible()).append(", with17Compatible=");
      log.append(comp.isWith17Compatible());
      logger.debug(log.toString());
    }

    SVNRepository svnRepository = null;

    try
    {
      SVNURL url = SVNRepositoryFactory.createLocalRepository(directory, null,
                     true, false, comp.isPre14Compatible(),
                     comp.isPre15Compatible(), comp.isPre16Compatible(),
                     comp.isPre17Compatible(), comp.isWith17Compatible());

      svnRepository = SVNRepositoryFactory.create(url);

      String uuid = svnRepository.getRepositoryUUID(true);

      if (Util.isNotEmpty(uuid))
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("store repository uuid {} for {}", uuid,
            repository.getName());
        }

        repository.setProperty(PROPERTY_UUID, uuid);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not read repository uuid for {}",
          repository.getName());
      }
    }
    catch (SVNException ex)
    {
      logger.error("could not create svn repository", ex);
      throw new InternalRepositoryException(entity(repository), "could not create repository", ex);
    }
    finally
    {
      SvnUtil.closeSession(svnRepository);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected SvnConfig createInitialConfig()
  {
    return new SvnConfig();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<SvnConfig> getConfigClass()
  {
    return SvnConfig.class;
  }

  @Override
  protected void postCreate(Repository repository, File directory) throws IOException {
    INISection iniSection = new INISection(CONFIG_SECTION_SCMM);
    iniSection.setParameter(CONFIG_KEY_REPOSITORY_ID, repository.getId());
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.addSection(iniSection);
    new INIConfigurationWriter().write(iniConfiguration, new File(directory, CONFIG_FILE_NAME));
  }

  String getRepositoryId(File directory) {
    try {
      return new INIConfigurationReader().read(new File(directory, CONFIG_FILE_NAME)).getSection(CONFIG_SECTION_SCMM).getParameter(CONFIG_KEY_REPOSITORY_ID);
    } catch (IOException e) {
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity("Directory", directory.toString()), "could not read scm configuration file", e);
    }
  }
}
