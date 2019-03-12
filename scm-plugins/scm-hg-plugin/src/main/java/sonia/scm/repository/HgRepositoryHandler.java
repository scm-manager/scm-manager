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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConfigurationException;
import sonia.scm.ContextEntry;
import sonia.scm.SCMContextProvider;
import sonia.scm.installer.HgInstaller;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.spi.HgRepositoryServiceProvider;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SystemUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Extension
public class HgRepositoryHandler
  extends AbstractSimpleRepositoryHandler<HgConfig>
{

  /** Field description */
  public static final String PATH_HOOK = ".hook-1.8";

  /** Field description */
  public static final String RESOURCE_VERSION =
    "sonia/scm/version/scm-hg-plugin";

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Mercurial";

  /** Field description */
  public static final String TYPE_NAME = "hg";

  /** Field description */
  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME,
                                    TYPE_DISPLAYNAME,
                                    HgRepositoryServiceProvider.COMMANDS,
                                    HgRepositoryServiceProvider.FEATURES);

  /** the logger for HgRepositoryHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(HgRepositoryHandler.class);

  /** Field description */
  public static final String PATH_HGRC =
    ".hg".concat(File.separator).concat("hgrc");
  private static final String CONFIG_SECTION_SCMM = "scmm";
  private static final String CONFIG_KEY_REPOSITORY_ID = "repositoryid";

  //~--- constructors ---------------------------------------------------------

  @Inject
  public HgRepositoryHandler(ConfigurationStoreFactory storeFactory,
                             Provider<HgContext> hgContextProvider,
                             RepositoryLocationResolver repositoryLocationResolver,
                             PluginLoader pluginLoader)
  {
    super(storeFactory, repositoryLocationResolver, pluginLoader);
    this.hgContextProvider = hgContextProvider;

    try
    {
      this.jaxbContext = JAXBContext.newInstance(BrowserResult.class,
        BlameResult.class, Changeset.class, ChangesetPagingResult.class,
        HgVersion.class);
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException("could not create jaxbcontext", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param autoConfig
   */
  public void doAutoConfiguration(HgConfig autoConfig)
  {
    HgInstaller installer = HgInstallerFactory.createInstaller();

    try
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("installing mercurial with {}",
          installer.getClass().getName());
      }

      installer.install(baseDirectory, autoConfig);
      config = autoConfig;
      storeConfig();
    }
    catch (IOException ioe)
    {
      if (logger.isErrorEnabled())
      {
        logger.error("Could not write Hg CGI for inital config.  "
          + "HgWeb may not function until a new Hg config is set", ioe);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    super.init(context);
    writePythonScripts(context);

    // fix wrong hg.bat from package installation
    if (SystemUtil.isWindows())
    {
      HgWindowsPackageFix.fixHgPackage(context, getConfig());
    }
  }

  /**
   * Method description
   *
   */
  @Override
  public void loadConfig()
  {
    super.loadConfig();

    if (config == null)
    {
      doAutoConfiguration(new HgConfig());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public HgContext getHgContext()
  {
    HgContext context = hgContextProvider.get();

    if (context == null)
    {
      context = new HgContext();
    }

    return context;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ImportHandler getImportHandler()
  {
    return new HgImportHandler(this);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public JAXBContext getJaxbContext()
  {
    return jaxbContext;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryType getType()
  {
    return TYPE;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getVersionInformation()
  {
    String version = getStringFromResource(RESOURCE_VERSION,
                       DEFAULT_VERSION_INFORMATION);

    try
    {
      HgVersion hgVersion = new HgVersionHandler(this, hgContextProvider.get(),
                              baseDirectory).getVersion();

      if (hgVersion != null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("mercurial/python informations: {}", hgVersion);
        }

        version = MessageFormat.format(version, hgVersion.getPython(),
          hgVersion.getMercurial());
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not retrieve version informations");
      }
    }
    catch (Exception ex)
    {
      logger.error("could not read version informations", ex);
    }

    return version;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @return
   */
  @Override
  protected ExtendedCommand buildCreateCommand(Repository repository,
    File directory)
  {
    ExtendedCommand cmd = new ExtendedCommand(config.getHgBinary(), "init",
                            directory.getAbsolutePath());

    // copy system environment, because of the PATH variable
    cmd.setUseSystemEnvironment(true);

    // issue-97
    cmd.setWorkDirectory(baseDirectory);

    return cmd;
  }

  /**
   * Writes .hg/hgrc, disables hg access control and added scm hook support.
   * see HgPermissionFilter
   *
   * @param repository
   * @param directory
   *
   * @throws IOException
   */
  @Override
  protected void postCreate(Repository repository, File directory)
    throws IOException
  {
    File hgrcFile = new File(directory, PATH_HGRC);
    INIConfiguration hgrc = new INIConfiguration();

    INISection iniSection = new INISection(CONFIG_SECTION_SCMM);
    iniSection.setParameter(CONFIG_KEY_REPOSITORY_ID, repository.getId());
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.addSection(iniSection);
    hgrc.addSection(iniSection);

    INIConfigurationWriter writer = new INIConfigurationWriter();

    writer.write(hgrc, hgrcFile);
  }

  public String getRepositoryId(File directory) {
    try {
      return new INIConfigurationReader().read(new File(directory, PATH_HGRC)).getSection(CONFIG_SECTION_SCMM).getParameter(CONFIG_KEY_REPOSITORY_ID);
    } catch (IOException e) {
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity("Directory", directory.toString()), "could not read scm configuration file", e);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<HgConfig> getConfigClass()
  {
    return HgConfig.class;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   */
  private void writePythonScripts(SCMContextProvider context)
  {
    IOUtil.mkdirs(HgPythonScript.getScriptDirectory(context));

    for (HgPythonScript script : HgPythonScript.values())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("write python script {}", script.getName());
      }

      InputStream content = null;
      OutputStream output = null;

      try
      {
        content = HgRepositoryHandler.class.getResourceAsStream(
          script.getResourcePath());
        output = new FileOutputStream(script.getFile(context));
        IOUtil.copy(content, output);
      }
      catch (IOException ex)
      {
        logger.error("could not write script", ex);
      }
      finally
      {
        IOUtil.close(content);
        IOUtil.close(output);
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Provider<HgContext> hgContextProvider;

  /** Field description */
  private JAXBContext jaxbContext;
}
