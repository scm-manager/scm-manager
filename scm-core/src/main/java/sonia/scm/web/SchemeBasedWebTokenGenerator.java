/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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

import com.google.common.base.Strings;

import org.apache.shiro.authc.AuthenticationToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public abstract class SchemeBasedWebTokenGenerator implements WebTokenGenerator
{

  /** authorization header */
  private static final String HEADER_AUTHORIZATION = "Authorization";

  /**
   * the logger for SchemeBasedWebTokenGenerator
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SchemeBasedWebTokenGenerator.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param scheme
   * @param authorization
   *
   * @return
   */
  protected abstract AuthenticationToken createToken(
    HttpServletRequest request, String scheme, String authorization);

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  public AuthenticationToken createToken(HttpServletRequest request)
  {
    AuthenticationToken token = null;
    String authorization = request.getHeader(HEADER_AUTHORIZATION);

    if (!Strings.isNullOrEmpty(authorization))
    {
      String[] parts = authorization.split("\\s+");

      if (parts.length > 0)
      {
        token = createToken(request, parts[0], parts[1]);

        if (token == null)
        {
          logger.warn("could not create token from authentication header");
        }
      }
      else
      {
        logger.warn("found malformed authentication header");
      }
    }

    return token;
  }
}
