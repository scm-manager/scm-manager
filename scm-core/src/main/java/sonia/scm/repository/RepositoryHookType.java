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

/**
 * Type of repository hook.
 *
 * @since 1.6
 */
public enum RepositoryHookType
{

  /**
   * A pre receive hook is invoked when new changesets are pushed to the
   * repository, but they are not yet stored in the repository. A pre receive 
   * hook can be used to reject the new incoming changesets.
   * 
   * @since 1.8
   */
  PRE_RECEIVE,

  /**
   * A post receive hook is invoked when new changesets are stored in the
   * repository.
   *
   * @since 1.6
   */
  POST_RECEIVE
}
