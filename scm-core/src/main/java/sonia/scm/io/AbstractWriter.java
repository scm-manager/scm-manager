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


import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractWriter<T>
{
  public abstract void write(T object, OutputStream output) throws IOException;

  public void write(T object, File file) throws IOException
  {
    OutputStream output = null;

    try
    {
      output = new FileOutputStream(file);
      write(object, output);
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  public void write(T object, String path) throws IOException
  {
    write(object, new File(path));
  }
}
