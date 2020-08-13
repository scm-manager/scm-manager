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

package sonia.scm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import java.util.Set;

/**
 * Jwt implementation of {@link AccessTokenResolver}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public final class JwtAccessTokenResolver implements AccessTokenResolver {

  /**
   * the logger for JwtAccessTokenResolver
   */
  private static final Logger LOG = LoggerFactory.getLogger(JwtAccessTokenResolver.class);

  private final SecureKeyResolver keyResolver;
  private final Set<AccessTokenValidator> validators;

  @Inject
  public JwtAccessTokenResolver(SecureKeyResolver keyResolver, Set<AccessTokenValidator> validators) {
    this.keyResolver = keyResolver;
    this.validators = validators;
  }

  @Override
  public JwtAccessToken resolve(BearerToken bearerToken) {
    try {
      String compact = bearerToken.getCredentials();

      Claims claims = Jwts.parser()
        .setSigningKeyResolver(keyResolver)
        .parseClaimsJws(compact)
        .getBody();

      JwtAccessToken token = new JwtAccessToken(claims, compact);
      validate(token);

      return token;
    } catch (ExpiredJwtException ex) {
      throw new TokenExpiredException("The jwt token has been expired", ex);
    } catch (JwtException ex) {
      throw new AuthenticationException("signature is invalid", ex);
    }
  }


  private void validate(AccessToken accessToken) {
    validators.forEach(validator -> validate(validator, accessToken));
  }

  private void validate(AccessTokenValidator validator, AccessToken accessToken) {
    if (!validator.validate(accessToken)) {
      String msg = createValidationFailedMessage(validator, accessToken);
      LOG.debug(msg);
      throw new AuthenticationException(msg);
    }
  }

  private String createValidationFailedMessage(AccessTokenValidator validator, AccessToken accessToken) {
    return String.format("token %s is invalid, marked by validator %s", accessToken.getId(), validator.getClass());
  }

}
