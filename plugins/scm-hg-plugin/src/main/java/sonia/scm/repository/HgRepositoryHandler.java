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

import org.slf4j.LoggerFactory;

import sonia.scm.Type;
import sonia.scm.installer.HgInstaller;
import sonia.scm.installer.UnixHgInstaller;
import sonia.scm.installer.WindowsHgInstaller;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.SystemUtil;
import sonia.scm.web.HgWebConfigWriter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import sonia.scm.io.FileSystem;

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
  public static final String TYPE_DISPLAYNAME = "Mercurial";

  /** Field description */
  public static final String TYPE_NAME = "hg";

  /** Field description */
  public static final Type TYPE = new Type(TYPE_NAME, TYPE_DISPLAYNAME);

  /** the logger for HgRepositoryHandler */
  private static final org.slf4j.Logger logger =
    LoggerFactory.getLogger(HgRepositoryHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   */
  @Inject
  public HgRepositoryHandler(StoreFactory storeFactory, FileSystem fileSystem)
  {
    super(storeFactory, fileSystem);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void doAutoConfiguration()
  {
    HgInstaller installer = null;

    if (SystemUtil.isWindows())
    {
      installer = new WindowsHgInstaller(baseDirectory);
    }
    else
    {
      installer = new UnixHgInstaller(baseDirectory);
    }

    if (config == null)
    {
      config = new HgConfig();

      try
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("installing mercurial with {}",
                       installer.getClass().getName());
        }

        installer.install(config);
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
    else
    {
      try
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("update mercurial with {}",
                       installer.getClass().getName());
        }

        installer.update(config);
      }
      catch (IOException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    storeConfig();
  }

  /**
   * Method description
   *
   */
  @Override
  public void loadConfig()
  {
    super.loadConfig();
    doAutoConfiguration();
  }

  //~--- get methods ----------------------------------------------------------

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
   * Writes .hg/hgrc and disables hg access control
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
    File hgrcFile = new File(directory,
                             ".hg".concat(File.separator).concat("hgrc"));
    INIConfiguration hgrc = new INIConfiguration();
    INISection webSection = new INISection("web");

    webSection.setParameter("push_ssl", "false");
    webSection.setParameter("allow_read", "*");
    webSection.setParameter("allow_push", "*");
    hgrc.addSection(webSection);

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
}
