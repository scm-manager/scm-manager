/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConfigurationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.installer.HgInstaller;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.spi.HgRepositoryServiceProvider;
import sonia.scm.repository.spi.HgWorkdirFactory;
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
                             PluginLoader pluginLoader, HgWorkdirFactory workdirFactory)
  {
    super(storeFactory, repositoryLocationResolver, pluginLoader);
    this.hgContextProvider = hgContextProvider;
    this.workdirFactory = workdirFactory;

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

  public HgWorkdirFactory getWorkdirFactory() {
    return workdirFactory;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Provider<HgContext> hgContextProvider;

  /** Field description */
  private JAXBContext jaxbContext;

  private final HgWorkdirFactory workdirFactory;
}
