/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.web.cgi;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for handling return codes of processes
 * executed by the {@link CGIExecutor}.
 *
 * @author Sebastian Sdorra
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
