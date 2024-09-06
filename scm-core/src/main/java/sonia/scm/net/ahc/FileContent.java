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

package sonia.scm.net.ahc;


import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sets the content of the file to the request.
 *
 * @since 1.46
 */
public class FileContent implements Content
{
  private final File file;

  public FileContent(File file)
  {
    this.file = file;
  }


  /**
   * Sets the content length of the file as request header.
   *
   *
   * @param request request
   */
  @Override
  public void prepare(AdvancedHttpRequestWithBody request)
  {
    request.contentLength(file.length());
  }

  /**
   * Copies the content of the file to the output stream.
   *
   *
   * @param output output stream
   *
   * @throws IOException
   */
  @Override
  public void process(OutputStream output) throws IOException
  {
    InputStream stream = null;

    try
    {
      stream = new FileInputStream(file);
      ByteStreams.copy(stream, output);
    }
    finally
    {
      Closeables.close(stream, true);
    }
  }
}
