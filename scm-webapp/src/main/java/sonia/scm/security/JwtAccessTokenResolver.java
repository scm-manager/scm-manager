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

package sonia.scm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import java.util.Set;

/**
 * Jwt implementation of {@link AccessTokenResolver}.
 *
 * @since 2.0.0
 */
@Extension
public final class JwtAccessTokenResolver implements AccessTokenResolver {

 
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
      throw new TokenValidationFailedException(validator, accessToken);
    }
  }

  private String createValidationFailedMessage(AccessTokenValidator validator, AccessToken accessToken) {
    return String.format("token %s is invalid, marked by validator %s", accessToken.getId(), validator.getClass());
  }

}
