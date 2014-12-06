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



package sonia.scm.web.filter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper methods for implementations of the {@link AutoLoginModule}.
 *
 * @author Sebastian Sdorra <sebastian.sdorra@gmail.com>
 *
 * @since 1.42
 */
public final class AutoLoginModules
{

  /** Field description */
  private static final String FLAG_COMPLETE =
    AutoLoginModules.class.getName().concat("complete");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private AutoLoginModules() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Mark the request as completed. No further actions will be executed.
   *
   * @param request http servlet request
   */
  public static void markAsComplete(HttpServletRequest request)
  {
    request.setAttribute(FLAG_COMPLETE, Boolean.TRUE);
  }

  /**
   * Sends a redirect to the specified url and marks the request as completed.
   * This method is useful for SSO solutions which have to redirect the user
   * to a central login page. This method must be used in favor of
   * {@link HttpServletResponse#sendRedirect(java.lang.String)} which could
   * result in an error.
   *
   * @param request http servlet request
   * @param response http servlet response
   * @param url redirect target
   *
   * @throws IOException if client could not be redirected
   */
  public static void sendRedirect(HttpServletRequest request,
    HttpServletResponse response, String url)
    throws IOException
  {
    markAsComplete(request);
    response.sendRedirect(url);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} is the request is marked as complete.
   *
   * @param request http servlet request
   *
   * @return {@code true} if request is complete
   */
  public static boolean isComplete(HttpServletRequest request)
  {
    return request.getAttribute(FLAG_COMPLETE) != null;
  }
}
