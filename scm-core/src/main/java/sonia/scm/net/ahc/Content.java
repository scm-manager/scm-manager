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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Content of a {@link AdvancedHttpRequestWithBody}.
 *
 * @since 1.46
 */
public interface Content
{

  /**
   * Prepares the {@link AdvancedHttpRequestWithBody} for the request content.
   * Implementations can set the content type, content length or custom headers
   * for the request.
   *
   *
   * @param request request
   * 
   * @throws IOException
   */
  public void prepare(AdvancedHttpRequestWithBody request) throws IOException;

  /**
   * Copies the content to the output stream.
   *
   *
   * @param output output stream
   *
   * @throws IOException
   */
  public void process(OutputStream output) throws IOException;
}
