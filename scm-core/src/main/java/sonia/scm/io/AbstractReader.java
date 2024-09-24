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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractReader<T>
{

  public abstract T read(InputStream input) throws IOException;

  public T read(byte[] data) throws IOException
  {
    return read(new ByteArrayInputStream(data));
  }

  public T read(String path) throws IOException
  {
    return read(new File(path));
  }

  public T read(File file) throws IOException
  {
    T result = null;
    InputStream input = null;

    try
    {
      input = new FileInputStream(file);
      result = read(input);
    }
    finally
    {
      IOUtil.close(input);
    }

    return result;
  }
}
