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

package sonia.scm.web;


import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priority;
import sonia.scm.plugin.Extension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Creates a {@link UsernamePasswordToken} from an authorization header with
 * basic authentication.
 *
 * @since 2.0.0
 */
@Priority(100)
@Extension
public class BasicWebTokenGenerator extends SchemeBasedWebTokenGenerator {

  @VisibleForTesting
  static final String BEARER_TOKEN_IDENTIFIER = "__bearer_token";

  /** credential separator for basic authentication */
  private static final String CREDENTIAL_SEPARATOR = ":";

  /** default encoding to decode basic authentication header */
  private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;

 
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
  protected AuthenticationToken createToken(HttpServletRequest request, String scheme, String authorization)
  {
    AuthenticationToken authToken = null;

    if (HttpUtil.AUTHORIZATION_SCHEME_BASIC.equalsIgnoreCase(scheme))
    {
      String token = decodeAuthenticationHeader(request, authorization);

      int index = token.indexOf(CREDENTIAL_SEPARATOR);

      if ((index > 0) && (index < token.length()))
      {
        String username = token.substring(0, index);
        String password = token.substring(index + 1);

        authToken = createTokenFromCredentials(request, username, password);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("failed to read basic auth credentials");
      }
    }

    return authToken;
  }

  private AuthenticationToken createTokenFromCredentials(HttpServletRequest request, String username, String password) {
    if (Util.isNotEmpty(username) && Util.isNotEmpty(password)) {
      if (BEARER_TOKEN_IDENTIFIER.equals(username)) {
        logger.trace("create bearer token");
        return BearerToken.create(SessionId.from(request).orElse(null), password);
      } else {
        logger.trace("create username password token for {}", username);
        return new UsernamePasswordToken(username, password);
      }
    } else if (logger.isWarnEnabled()) {
      logger.warn("username or password is null/empty");
    }
    return null;
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
