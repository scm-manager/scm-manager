/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.i18n.Bundle;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.cgi.CGIExceptionHandler;
import sonia.scm.web.cgi.CGIStatusCodeHandler;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgCGIExceptionHandler
        implements CGIExceptionHandler, CGIStatusCodeHandler
{

  /** Field description */
  public static final String BUNDLE_PATH = "sonia.scm.web.cgimessages";

  /** Field description */
  public static final String CONTENT_TYPE_ERROR = "application/hg-error";

  /** TODO create a bundle for error messages */
  public static final String ERROR_NOT_CONFIGURED = "error.notConfigured";

  /** Field description */
  public static final String ERROR_STATUSCODE = "error.statusCode";

  /** Field description */
  public static final String ERROR_UNEXPECTED = "error.unexpected";

  /**
   * the logger for HgCGIExceptionHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgCGIExceptionHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public HgCGIExceptionHandler()
  {
    this.bundle = Bundle.getBundle(BUNDLE_PATH);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param ex
   */
  @Override
  public void handleException(HttpServletRequest request,
                              HttpServletResponse response, Throwable ex)
  {
    if (logger.isErrorEnabled())
    {
      logger.error("not able to handle mercurial request", ex);
    }

    sendError(response,
              bundle.getString(ERROR_UNEXPECTED, Util.nonNull(ex.getMessage())));
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param output
   * @param statusCode
   *
   * @throws IOException
   */
  @Override
  public void handleStatusCode(HttpServletRequest request,
                               HttpServletResponse response,
                               OutputStream output, int statusCode)
          throws IOException
  {
    if (statusCode != 0)
    {
      response.setContentType(CONTENT_TYPE_ERROR);

      String msg = bundle.getLine(ERROR_STATUSCODE, statusCode);

      if (logger.isWarnEnabled())
      {
        logger.warn(msg);
      }

      output.write(msg.getBytes(Charsets.US_ASCII));
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("mercurial process ends successfully");
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param statusCode
   */
  @Override
  public void handleStatusCode(HttpServletRequest request, int statusCode)
  {
    if (statusCode != 0)
    {
      if (logger.isWarnEnabled())
      {
        logger.error("mercurial process ends with {}", statusCode);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("mercurial process ends successfully");
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param message
   *
   */
  public void sendError(HttpServletResponse response, String message)
  {
    response.setContentType(CONTENT_TYPE_ERROR);

    PrintWriter writer = null;

    try
    {
      writer = response.getWriter();
      writer.println(message);
    }
    catch (IOException ex)
    {
      logger.error("could not write error message to client", ex);
    }
    finally
    {
      IOUtil.close(writer);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param key
   */
  public void sendFormattedError(HttpServletResponse response, String key)
  {
    sendError(response, bundle.getString(key));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Bundle bundle;
}
