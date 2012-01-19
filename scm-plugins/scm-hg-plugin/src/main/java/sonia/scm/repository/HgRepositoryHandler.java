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
import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.installer.HgInstaller;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.io.DirectoryFileFilter;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.FileSystem;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;
import sonia.scm.web.HgWebConfigWriter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

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
  public static final String TYPE_DISPLAYNAME = "Mercurial";

  /** Field description */
  public static final String TYPE_NAME = "hg";

  /** Field description */
  public static final Type TYPE = new Type(TYPE_NAME, TYPE_DISPLAYNAME);

  /** the logger for HgRepositoryHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(HgRepositoryHandler.class);

  /** Field description */
  public static final String PATH_HGRC =
    ".hg".concat(File.separator).concat("hgrc");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   * @param fileSystem
   * @param hgContextProvider
   */
  @Inject
  public HgRepositoryHandler(StoreFactory storeFactory, FileSystem fileSystem,
                             Provider<HgContext> hgContextProvider)
  {
    super(storeFactory, fileSystem);
    this.hgContextProvider = hgContextProvider;

    try
    {
      this.browserResultContext = JAXBContext.newInstance(BrowserResult.class);
      this.blameResultContext = JAXBContext.newInstance(BlameResult.class);
      this.changesetContext = JAXBContext.newInstance(Changeset.class);
      this.changesetPagingResultContext =
        JAXBContext.newInstance(ChangesetPagingResult.class);
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
      new HgWebConfigWriter(config).write();
    }
    catch (IOException ioe)
    {
      if (logger.isErrorEnabled())
      {
        logger.error(
            "Could not write Hg CGI for inital config.  "
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
    registerMissingHooks();
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
   * @param repository
   *
   * @return
   */
  @Override
  public BlameViewer getBlameViewer(Repository repository)
  {
    BlameViewer blameViewer = null;

    AssertUtil.assertIsNotNull(repository);

    String type = repository.getType();

    AssertUtil.assertIsNotEmpty(type);

    if (TYPE_NAME.equals(type))
    {
      blameViewer = new HgBlameViewer(this, blameResultContext,
                                      hgContextProvider.get(), repository);
    }
    else
    {
      throw new IllegalArgumentException("mercurial repository is required");
    }

    return blameViewer;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  public ChangesetViewer getChangesetViewer(Repository repository)
  {
    HgChangesetViewer changesetViewer = null;

    AssertUtil.assertIsNotNull(repository);

    String type = repository.getType();

    AssertUtil.assertIsNotEmpty(type);

    if (TYPE_NAME.equals(type))
    {
      changesetViewer = new HgChangesetViewer(this,
              changesetPagingResultContext, changesetContext,
              hgContextProvider.get(), repository);
    }
    else
    {
      throw new IllegalArgumentException("mercurial repository is required");
    }

    return changesetViewer;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws RepositoryException
   */
  @Override
  public DiffViewer getDiffViewer(Repository repository)
          throws RepositoryException
  {
    DiffViewer diffViewer = null;

    AssertUtil.assertIsNotNull(repository);

    String type = repository.getType();

    AssertUtil.assertIsNotEmpty(type);

    if (TYPE_NAME.equals(type))
    {
      diffViewer = new HgDiffViewer(this, hgContextProvider.get(), repository);
    }
    else
    {
      throw new IllegalArgumentException("mercurial repository is required");
    }

    return diffViewer;
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
   * @param repository
   *
   * @return
   */
  @Override
  public RepositoryBrowser getRepositoryBrowser(Repository repository)
  {
    return new HgRepositoryBrowser(this, browserResultContext,
                                   hgContextProvider.get(), repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Type getType()
  {
    return TYPE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param hgrc
   */
  void appendHookSection(INIConfiguration hgrc)
  {
    INISection hooksSection = new INISection("hooks");

    setHookParameter(hooksSection);
    hgrc.addSection(hooksSection);
  }

  /**
   * Method description
   *
   *
   * @param hgrc
   */
  void appendWebSection(INIConfiguration hgrc)
  {
    INISection webSection = new INISection("web");

    setWebParameter(webSection);
    hgrc.addSection(webSection);
  }

  /**
   * Method description
   *
   *
   * @param c
   * @param repositoryName
   *
   * @return
   */
  boolean registerMissingHook(INIConfiguration c, String repositoryName)
  {
    INISection hooks = c.getSection("hooks");

    if (hooks == null)
    {
      hooks = new INISection("hooks");
      c.addSection(hooks);
    }

    boolean write = false;

    if (appendHook(repositoryName, hooks, "changegroup.scm"))
    {
      write = true;
    }

    if (appendHook(repositoryName, hooks, "pretxnchangegroup.scm"))
    {
      write = true;
    }

    return write;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositoryDirectory
   *
   * @return
   */
  HgChangesetViewer getChangesetViewer(File repositoryDirectory)
  {
    AssertUtil.assertIsNotNull(repositoryDirectory);

    if (!repositoryDirectory.isDirectory())
    {
      throw new IllegalStateException("directory not found");
    }

    return new HgChangesetViewer(this, changesetPagingResultContext,
                                 changesetContext, hgContextProvider.get(),
                                 repositoryDirectory);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param hooksSection
   */
  void setHookParameter(INISection hooksSection)
  {
    hooksSection.setParameter("changegroup.scm", "python:scmhooks.callback");
    hooksSection.setParameter("pretxnchangegroup.scm",
                              "python:scmhooks.callback");
  }

  /**
   * Method description
   *
   *
   * @param webSection
   */
  void setWebParameter(INISection webSection)
  {
    webSection.setParameter("push_ssl", "false");
    webSection.setParameter("allow_read", "*");
    webSection.setParameter("allow_push", "*");
  }

  //~--- methods --------------------------------------------------------------

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
    return new ExtendedCommand(config.getHgBinary(), "init",
                               directory.getPath());
  }

  /**
   * Writes .hg/hgrc, disables hg access control and added scm hook support.
   * see HgPermissionFilter
   *
   * @param repository
   * @param directory
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  protected void postCreate(Repository repository, File directory)
          throws IOException, RepositoryException
  {
    File hgrcFile = new File(directory, PATH_HGRC);
    INIConfiguration hgrc = new INIConfiguration();

    appendWebSection(hgrc);

    // register hooks
    appendHookSection(hgrc);

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
   * @param repositoryName
   * @param hooks
   * @param hookName
   *
   * @return
   */
  private boolean appendHook(String repositoryName, INISection hooks,
                             String hookName)
  {
    boolean write = false;
    String hook = hooks.getParameter(hookName);

    if (Util.isEmpty(hook))
    {
      if (logger.isInfoEnabled())
      {
        logger.info("register missing {} hook for respository {}", hookName,
                    repositoryName);
      }

      hooks.setParameter(hookName, "python:scmhooks.callback");
      write = true;
    }

    return write;
  }

  /**
   * Method description
   *
   *
   * @param file
   */
  private void createNewFile(File file)
  {
    try
    {
      if (!file.createNewFile() && logger.isErrorEnabled())
      {
        logger.error("could not create file {}", file);
      }
    }
    catch (IOException ex)
    {
      logger.error("could not create file {}".concat(file.getPath()), ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param repositoryDir
   *
   * @return
   */
  private boolean registerMissingHook(File repositoryDir)
  {
    boolean result = false;
    File hgrc = new File(repositoryDir, PATH_HGRC);

    if (hgrc.exists())
    {
      try
      {
        INIConfigurationReader reader = new INIConfigurationReader();
        INIConfiguration c = reader.read(hgrc);
        String repositoryName = repositoryDir.getName();

        if (registerMissingHook(c, repositoryName))
        {
          if (logger.isDebugEnabled())
          {
            logger.debug("rewrite hgrc for repository {}", repositoryName);
          }

          INIConfigurationWriter writer = new INIConfigurationWriter();

          writer.write(c, hgrc);
        }

        result = true;
      }
      catch (IOException ex)
      {
        logger.error("could not register missing hook", ex);
      }
    }

    return result;
  }

  /**
   * Method description
   *
   */
  private void registerMissingHooks()
  {
    HgConfig c = getConfig();

    if (c != null)
    {
      File repositoryDirectroy = c.getRepositoryDirectory();

      if (repositoryDirectroy.exists())
      {
        File lockFile = new File(repositoryDirectroy, PATH_HOOK);

        if (!lockFile.exists())
        {
          File[] dirs =
            repositoryDirectroy.listFiles(DirectoryFileFilter.instance);
          boolean success = true;

          if (Util.isNotEmpty(dirs))
          {
            for (File dir : dirs)
            {
              if (!registerMissingHook(dir))
              {
                success = false;
              }
            }
          }

          if (success)
          {
            createNewFile(lockFile);
          }
        }
        else if (logger.isDebugEnabled())
        {
          logger.debug("hooks allready registered");
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug(
            "repository directory does not exists, could not register missing hooks");
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("config is not available, could not register missing hooks");
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext blameResultContext;

  /** Field description */
  private JAXBContext browserResultContext;

  /** Field description */
  private JAXBContext changesetContext;

  /** Field description */
  private JAXBContext changesetPagingResultContext;

  /** Field description */
  private Provider<HgContext> hgContextProvider;
}
