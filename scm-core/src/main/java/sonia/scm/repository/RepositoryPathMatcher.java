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

package sonia.scm.repository;

import sonia.scm.plugin.ExtensionPoint;

/**
 * ExtensionPoint to modify the path matching behaviour for a certain type of repositories.
 * 
 * @since 1.54
 */
@ExtensionPoint
public interface RepositoryPathMatcher {
  
  /**
   * Returns {@code true} if the path matches the repository.
   * 
   * @param repository repository
   * @param path requested path without context and without type information extracted from uri
   * 
   * @return {@code true} if the path matches
   */
  boolean isPathMatching(Repository repository, String path);
  
  /**
   * Returns the type of repository for which the matcher is responsible.
   */
  String getType();
}
