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
 * Features which are supported by a {@link Repository}.
 *
 * @since 1.25
 */
public enum Feature
{

  /**
   * The default branch of the repository is a combined branch of all
   * repository branches.
   */
  COMBINED_DEFAULT_BRANCH,
  /**
   * The repository supports computation of incoming changes (either diff or list of changesets) of one branch
   * in respect to another target branch.
   */
  INCOMING_REVISION,
  /**
   * The repository supports computation of modifications between two revisions, not only for a singe revision.
   *
   * @since 2.23.0
   */
  MODIFICATIONS_BETWEEN_REVISIONS,
  /**
   * The repository supports pushing with a force flag.
   *
   * @since 2.47.0
   */
  FORCE_PUSH,
  /**
   * The repository supports computation of tags for a given revision.
   *
   * @since 3.1.0
   */
  TAGS_FOR_REVISION
}
