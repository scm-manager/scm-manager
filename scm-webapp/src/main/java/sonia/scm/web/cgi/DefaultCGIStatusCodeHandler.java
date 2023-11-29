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

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultCGIStatusCodeHandler implements CGIStatusCodeHandler
{

  /**
   * the logger for DefaultCGIStatusCodeHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultCGIStatusCodeHandler.class);

  //~--- methods --------------------------------------------------------------

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
      logger.warn("process ends with exit code {}", statusCode);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param ouputStream
   * @param statusCode
   *
   * @throws IOException
   */
  @Override
  public void handleStatusCode(HttpServletRequest request,
                               HttpServletResponse response,
                               OutputStream ouputStream, int statusCode)
          throws IOException
  {
    handleStatusCode(request, statusCode);
  }
}
