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

import java.util.concurrent.TimeUnit;

/**
 * The access token builder is able to create {@link AccessToken}. For more informations about access tokens have look
 * at {@link AccessToken}.
 * 
 * @since 2.0.0
 */
public interface AccessTokenBuilder {
  
  /**
   * Sets the subject for the token. 
   * If the subject is not set the currently authenticated subject will be used instead.
   * 
   * @param subject subject of token
   * 
   * @return * @return {@code this}
   */
  AccessTokenBuilder subject(String subject);
  
  /**
   * Adds a custom entry to the token.
   * 
   * @param key key of custom entry
   * @param value value of entry
   * 
   * @return {@code this}
   */
  AccessTokenBuilder custom(String key, Object value);
  
  /**
   * Sets the issuer for the token.
   * 
   * @param issuer issuer name or url
   * 
   * @return {@code this}
   */
  AccessTokenBuilder issuer(String issuer);
  
  /**
   * Sets the expiration for the token.
   * 
   * @param count expiration count
   * @param unit expiration unit
   * 
   * @return {@code this}
   */
  AccessTokenBuilder expiresIn(long count, TimeUnit unit);

  /**
   * Sets the time how long this token may be refreshed. Set this to 0 (zero) to disable automatic refresh.
   *
   * @param count Time unit count. If set to 0, automatic refresh is disabled.
   * @param unit time unit
   *
   * @return {@code this}
   */
  AccessTokenBuilder refreshableFor(long count, TimeUnit unit);

  /**
   * Reduces the permissions of the token by providing a scope.
   *
   * @param scope scope of token
   *
   * @return {@code this}
   */
  AccessTokenBuilder scope(Scope scope);

  /**
   * Creates a new {@link AccessToken} with the provided settings.
   * 
   * @return new {@link AccessToken}
   */
  AccessToken build();
  
}
