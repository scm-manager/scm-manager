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

import sonia.scm.plugin.ExtensionPoint;

/**
 * Validates an {@link AccessToken}. The validator is called during authentication
 * with an {@link AccessToken}.
 * 
 * @since 2.0.0
 */
@ExtensionPoint
public interface AccessTokenValidator {
  
  /**
   * Returns {@code true} if the {@link AccessToken} is valid. If the token is not valid and the
   * method returns {@code false}, the authentication is treated as failed.
   * 
   * @param token the access token to verify
   */
  boolean validate(AccessToken token);
}
