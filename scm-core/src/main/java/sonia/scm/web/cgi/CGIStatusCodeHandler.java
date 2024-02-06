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
    
package sonia.scm.web.cgi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for handling return codes of processes
 * executed by the {@link CGIExecutor}.
 *
 * @since 1.15
 */
public interface CGIStatusCodeHandler
{

  /**
   * Handles the return code of the process executed by {@link CGIExecutor}.
   * &lt;b&gt;Note:&lt;/b&gt; This method is called when the process has
   * already written to the {@link OutputStream}.
   *
   *
   * @param request the http request
   * @param statusCode process return code
   */
  public void handleStatusCode(HttpServletRequest request, int statusCode);

  /**
   * Handles the return code of the process executed by {@link CGIExecutor}.
   * &lt;b&gt;Note:&lt;/b&gt; This method is only called when the process has
   * not written to the {@link OutputStream}. Do not call
   * {@link HttpServletResponse#getWriter()}, because there was already a call
   * to {@link HttpServletResponse#getOutputStream()}.
   *
   *
   * @param request the http request
   * @param response the http response
   * @param ouputStream the servlet output stream
   * @param statusCode process return code
   *
   * @throws IOException
   */
  public void handleStatusCode(HttpServletRequest request,
                               HttpServletResponse response,
                               OutputStream ouputStream, int statusCode)
          throws IOException;
}
