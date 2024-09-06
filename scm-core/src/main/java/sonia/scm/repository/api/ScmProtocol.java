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

/**
 * An ScmProtocol represents a concrete protocol provided by the SCM-Manager instance
 * to interact with a repository depending on its type. There may be multiple protocols
 * available for a repository type (eg. http and ssh).
 */
public interface ScmProtocol {

  /**
   * The type of the concrete protocol, eg. "http" or "ssh".
   */
  String getType();

  /**
   * The URL to access the repository providing this protocol.
   */
  String getUrl();

  /**
   * Whether the protocol can be used as an anonymous user.
   */
  default boolean isAnonymousEnabled() {
    return true;
  }

  /**
   * Priority for frontend evaluation order
   *
   * @return priority number
   * @since 2.48.0
   */
  default int getPriority() {
    return 100;
  }
}
