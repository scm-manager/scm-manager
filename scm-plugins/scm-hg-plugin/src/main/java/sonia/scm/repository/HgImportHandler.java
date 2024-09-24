/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
