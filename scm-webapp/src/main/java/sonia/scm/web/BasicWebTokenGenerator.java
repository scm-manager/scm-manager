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

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

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
  private static final String CREDENTIAL_SEPARATOR = ":";

  /** default encoding to decode basic authentication header */
  private static final Charset DEFAULT_ENCODING = Charsets.ISO_8859_1;
  
  /**
   * the logger for BasicWebTokenGenerator
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BasicWebTokenGenerator.class);

  private final UserAgentParser userAgentParser;

  /**
   * Constructs a new BasicWebTokenGenerator.
   *
   * @param userAgentParser parser for user-agent header
   */
  @Inject
  public BasicWebTokenGenerator(UserAgentParser userAgentParser) {
    this.userAgentParser = userAgentParser;
  }

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
      String token = decodeAuthenticationHeader(request, authorization);

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
  
/**
   * Decode base64 of the basic authentication header. The method will use
   * the charset provided by the {@link UserAgent}, if the 
   * {@link UserAgentParser} is not available the method will be fall back to 
   * ISO-8859-1.
   *
   * @param request http request
   * @param authentication base64 encoded basic authentication string
   *
   * @return decoded basic authentication header
   *
   * @see <a href="http://goo.gl/tZEBS3">issue 627</a>
   * @see <a href="http://goo.gl/NhbZ2F">Stackoverflow Basic Authentication</a>
   *
   * @throws UnsupportedEncodingException
   */
  private String decodeAuthenticationHeader(HttpServletRequest request, String authentication)
  {
    Charset encoding = DEFAULT_ENCODING;

    if (userAgentParser != null)
    {
      encoding = userAgentParser.parse(request).getBasicAuthenticationCharset();
    }

    return new String(Base64.decode(authentication), encoding);
  }
}
