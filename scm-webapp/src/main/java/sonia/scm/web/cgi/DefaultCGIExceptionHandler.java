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
import sonia.scm.security.ScmSecurityException;


public class DefaultCGIExceptionHandler implements CGIExceptionHandler
{

  
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultCGIExceptionHandler.class);


 
  @Override
  public void handleException(HttpServletRequest request,
                              HttpServletResponse response, Throwable exception)
  {
    try
    {
      if (logger.isErrorEnabled())
      {
        logger.error("exception during cgi execution", exception);
      }

      if (exception instanceof ScmSecurityException)
      {
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                           exception.getMessage());
      }
      else
      {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           exception.getMessage());
      }
    }
    catch (Exception ex)
    {
      logger.error("could not handle exception", ex);
    }
  }
}
