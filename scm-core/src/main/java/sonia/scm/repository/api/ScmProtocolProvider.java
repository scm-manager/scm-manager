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

package sonia.scm.repository.api;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;

/**
 * Provider for scm native protocols.
 *
 * @param <T> type of protocol
 *
 * @since 2.0.0
 */
@ExtensionPoint(multi = true)
public interface ScmProtocolProvider<T extends ScmProtocol> {

  /**
   * Returns type of repository (e.g.: git, svn, hg, etc.)
   *
   * @return name of type
   */
  String getType();

  /**
   * Returns protocol for the given repository.
   *
   * @param repository repository
   *
   * @return protocol for repository
   */
  T get(Repository repository);
}
