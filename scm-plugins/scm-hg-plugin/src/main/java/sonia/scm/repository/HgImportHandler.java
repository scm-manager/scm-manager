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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INISection;
import sonia.scm.util.ValidationUtil;

import java.io.File;
import java.io.IOException;

/**
 *
 * @deprecated
 */
@Deprecated
public class HgImportHandler extends AbstactImportHandler
{

  public static final String HG_DIR = ".hg";

 
  private static final Logger logger =
    LoggerFactory.getLogger(HgImportHandler.class);

  private HgRepositoryHandler handler;
 
  public HgImportHandler(HgRepositoryHandler handler)
  {
    this.handler = handler;
  }


  @Override
  protected Repository createRepository(File repositoryDirectory,
          String repositoryName)
          throws IOException
  {
    Repository repository = super.createRepository(repositoryDirectory,
                              repositoryName);
    File hgrc = new File(repositoryDirectory, HgRepositoryHandler.PATH_HGRC);

    if (hgrc.exists())
    {
      INIConfigurationReader reader = new INIConfigurationReader();
      INIConfiguration c = reader.read(hgrc);
      INISection web = c.getSection("web");

      if (web != null) {
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
      }
    }
    else
    {
      handler.postCreate(repository, repositoryDirectory);
    }

    return repository;
  }


  
  @Override
  protected String[] getDirectoryNames()
  {
    return new String[] { HG_DIR };
  }

  
  @Override
  protected AbstractRepositoryHandler<?> getRepositoryHandler()
  {
    return handler;
  }

}
