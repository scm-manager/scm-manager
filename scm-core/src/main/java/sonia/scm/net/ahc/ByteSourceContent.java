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


import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link ByteSource} content for {@link AdvancedHttpRequestWithBody}.
 *
 * @since 1.46
 */
public class ByteSourceContent implements Content
{
  private final ByteSource byteSource;

  public ByteSourceContent(ByteSource byteSource)
  {
    this.byteSource = byteSource;
  }


  /**
   * Sets the content length for the request.
   *
   *
   * @param request http request
   *
   * @throws IOException
   */
  @Override
  public void prepare(AdvancedHttpRequestWithBody request) throws IOException
  {
    request.contentLength(byteSource.size());
  }

  /**
   * Copies the content of the byte source to the output stream.
   *
   *
   * @param output output stream
   *
   * @throws IOException
   */
  @Override
  public void process(OutputStream output) throws IOException
  {
    byteSource.copyTo(output);
  }

}
