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
    
package sonia.scm.web;


import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.i18n.Bundle;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.cgi.CGIExceptionHandler;
import sonia.scm.web.cgi.CGIStatusCodeHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;


public class HgCGIExceptionHandler
  implements CGIExceptionHandler, CGIStatusCodeHandler
{

  public static final String BUNDLE_PATH = "sonia.scm.web.cgimessages";

  public static final String CONTENT_TYPE_ERROR = "application/hg-error";

  public static final String CONTENT_TYPE_HTML = "text/html";

  /** TODO create a bundle for error messages */
  public static final String ERROR_NOT_CONFIGURED = "error.notConfigured";

  public static final String ERROR_STATUSCODE = "error.statusCode";

  public static final String ERROR_UNEXPECTED = "error.unexpected";

  private static final String HEADER_ACCEPT = "Accept";

 
  private static final Logger logger =
    LoggerFactory.getLogger(HgCGIExceptionHandler.class);


  public HgCGIExceptionHandler()
  {
    this.bundle = Bundle.getBundle(BUNDLE_PATH, Locale.ENGLISH,
      HgCGIExceptionHandler.class.getClassLoader());
  }


 
  @Override
  public void handleException(HttpServletRequest request,
    HttpServletResponse response, Throwable ex)
  {
    if (logger.isErrorEnabled())
    {
      logger.error("not able to handle mercurial request", ex);
    }

    //J-
    sendError(
      request,
      response, 
      bundle.getString(ERROR_UNEXPECTED, Util.nonNull(ex.getMessage()))
    );
    //J+
  }


  @Override
  public void handleStatusCode(HttpServletRequest request,
    HttpServletResponse response, OutputStream output, int statusCode)
    throws IOException
  {
    if (statusCode != 0)
    {
      setContentType(request, response);

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
   *
   * @param request
   * @param response
   * @param message
   *
   */
  public void sendError(HttpServletRequest request,
    HttpServletResponse response, String message)
  {
    setContentType(request, response);

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
   *
   * @param request
   * @param response
   * @param key
   */
  public void sendFormattedError(HttpServletRequest request,
    HttpServletResponse response, String key)
  {
    sendError(request, response, bundle.getString(key));
  }



  private void setContentType(HttpServletRequest request,
    HttpServletResponse response)
  {
    String accept = Strings.nullToEmpty(request.getHeader(HEADER_ACCEPT));

    if (accept.contains(CONTENT_TYPE_HTML))
    {
      response.setContentType(CONTENT_TYPE_HTML);
    }
    else
    {
      response.setContentType(CONTENT_TYPE_ERROR);
    }
  }

  //~--- fields ---------------------------------------------------------------

  private Bundle bundle;
}
