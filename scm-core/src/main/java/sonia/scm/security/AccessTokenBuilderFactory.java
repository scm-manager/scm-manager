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
 * Creates new {@link AccessTokenBuilder}. The AccessTokenBuilderFactory resolves all required dependencies for the 
 * access token builder. The builder factory is the main entry point for creating {@link AccessToken}.
 * 
 * @since 2.0.0
 */
@ExtensionPoint(multi = false)
public interface AccessTokenBuilderFactory {
  
  /**
   * Creates a new {@link AccessTokenBuilder}.
   * 
   * @return new {@link AccessTokenBuilder}
   */
  AccessTokenBuilder create();
}
