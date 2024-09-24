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

import org.apache.shiro.authc.AuthenticationException;

/**
 * Thrown by the {@link AccessTokenResolver} when an {@link AccessTokenValidator} fails to validate an access token.
 * @since 2.6.2
 */
@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class TokenValidationFailedException extends AuthenticationException {
  private final AccessTokenValidator validator;
  private final AccessToken accessToken;

  public TokenValidationFailedException(AccessTokenValidator validator, AccessToken accessToken) {
    super(String.format("Token validator %s failed for access token %s", validator.getClass(), accessToken.getId()));
    this.validator = validator;
    this.accessToken = accessToken;
  }

  public AccessTokenValidator getValidator() {
    return validator;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }
}
