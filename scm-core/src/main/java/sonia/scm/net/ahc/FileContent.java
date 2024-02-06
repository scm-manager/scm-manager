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
