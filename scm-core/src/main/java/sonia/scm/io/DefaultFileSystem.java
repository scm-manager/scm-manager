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

package sonia.scm.io;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;


public class DefaultFileSystem implements FileSystem
{
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultFileSystem.class);

  @Override
  public void create(File directory) throws IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("create directory {}", directory.getPath());
    }

    IOUtil.mkdirs(directory);
  }

  @Override
  public void destroy(File directory) throws IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("destroy directory {}", directory.getPath());
    }

    IOUtil.delete(directory);
  }
}
