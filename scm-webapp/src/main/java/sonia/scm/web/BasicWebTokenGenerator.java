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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.Priority;
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
@Priority(100)
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
