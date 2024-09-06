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
