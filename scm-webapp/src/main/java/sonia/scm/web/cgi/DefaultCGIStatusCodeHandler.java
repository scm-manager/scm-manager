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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;


public class DefaultCGIStatusCodeHandler implements CGIStatusCodeHandler
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultCGIStatusCodeHandler.class);



  @Override
  public void handleStatusCode(HttpServletRequest request, int statusCode)
  {
    if (statusCode != 0)
    {
      logger.warn("process ends with exit code {}", statusCode);
    }
  }


  @Override
  public void handleStatusCode(HttpServletRequest request,
                               HttpServletResponse response,
                               OutputStream ouputStream, int statusCode)
          throws IOException
  {
    handleStatusCode(request, statusCode);
  }
}
