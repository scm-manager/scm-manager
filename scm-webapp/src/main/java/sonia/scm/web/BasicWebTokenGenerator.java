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

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.Extension;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;

/**
 * Creates a {@link UsernamePasswordToken} from an authorization header with
 * basic authentication.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public class BasicWebTokenGenerator extends SchemeBasedWebTokenGenerator
{

  /** credential separator for basic authentication */
  public static final String CREDENTIAL_SEPARATOR = ":";

  /**
   * the logger for BasicWebTokenGenerator
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BasicWebTokenGenerator.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a {@link UsernamePasswordToken} from an authorization header with
   * basic authentication scheme.
   *
   *
   * @param request http servlet request
   * @param scheme authentication scheme
   * @param authorization authorization payload
   *
   * @return {@link UsernamePasswordToken} or {@code null}
   */
  @Override
  protected UsernamePasswordToken createToken(HttpServletRequest request,
    String scheme, String authorization)
  {
    UsernamePasswordToken authToken = null;

    if (HttpUtil.AUTHORIZATION_SCHEME_BASIC.equalsIgnoreCase(scheme))
    {
      String token = new String(Base64.decode(authorization.getBytes()));

      int index = token.indexOf(CREDENTIAL_SEPARATOR);

      if ((index > 0) && (index < token.length()))
      {
        String username = token.substring(0, index);
        String password = token.substring(index + 1);

        if (Util.isNotEmpty(username) && Util.isNotEmpty(password))
        {
          logger.trace("try to authenticate user {}", username);
          authToken = new UsernamePasswordToken(username, password);
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("username or password is null/empty");
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("failed to read basic auth credentials");
      }
    }

    return authToken;
  }
}
