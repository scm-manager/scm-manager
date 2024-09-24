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
 * AccessTokenResolver are used to parse, validate and verify an {@link AccessToken} from a {@link BearerToken}. The 
 * resolver should be used to get an {@link AccessToken} from a {@link BearerToken}.
 * 
 * @since 2.0.0
 */
@ExtensionPoint(multi = false)
public interface AccessTokenResolver {
  
  /**
   * Resolves an {@link AccessToken} from a {@link BearerToken}. The method will throw exceptions, if the given token
   * is expired, manipulated or invalid.
   * 
   * TODO specify exceptions
   * 
   * @param bearerToken bearer token
   * 
   * @return parsed {@link AccessToken}.
   */
  AccessToken resolve(BearerToken bearerToken);
  
}
