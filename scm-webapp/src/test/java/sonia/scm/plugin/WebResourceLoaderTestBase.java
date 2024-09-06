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

package sonia.scm.plugin;


import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;


public abstract class WebResourceLoaderTestBase
{

  /**
   * Method description
   *
   *
   * @param directory
   * @param path
   *
   * @return
   *
   * @throws IOException
   */
  protected File file(File directory, String path) throws IOException
  {
    IOUtil.mkdirs(directory);

    File file = new File(directory, path);

    Files.append("a", file, Charsets.UTF_8);

    return file;
  }

  //~--- fields ---------------------------------------------------------------

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
}
