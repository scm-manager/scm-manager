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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.util.ValidationUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgImportHandler extends AbstactImportHandler
{

  /** Field description */
  public static final String HG_DIR = ".hg";

  /**
   * the logger for HgImportHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgImportHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   */
  public HgImportHandler(HgRepositoryHandler handler)
  {
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositoryDirectory
   * @param repositoryName
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  protected Repository createRepository(File repositoryDirectory,
          String repositoryName)
          throws IOException, RepositoryException
  {
    Repository repository = super.createRepository(repositoryDirectory,
                              repositoryName);
    File hgrc = new File(repositoryDirectory, HgRepositoryHandler.PATH_HGRC);

    if (hgrc.exists())
    {
      INIConfigurationReader reader = new INIConfigurationReader();
      INIConfiguration c = reader.read(hgrc);
      INISection web = c.getSection("web");

      if (web == null)
      {
        handler.appendWebSection(c);
      }
      else
      {
        repository.setDescription(web.getParameter("description"));

        String contact = web.getParameter("contact");

        if (ValidationUtil.isMailAddressValid(contact))
        {
          repository.setContact(contact);
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("contact {} is not a valid mail address", contact);
        }

        handler.setWebParameter(web);
      }

      // issue-97
      handler.registerMissingHook(c, repositoryName);

      INIConfigurationWriter writer = new INIConfigurationWriter();

      writer.write(c, hgrc);
    }
    else
    {
      handler.postCreate(repository, repositoryDirectory);
    }

    return repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String[] getDirectoryNames()
  {
    return new String[] { HG_DIR };
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected AbstractRepositoryHandler<?> getRepositoryHandler()
  {
    return handler;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;
}
