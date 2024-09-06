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
 * Enumeration of available commands.
 *
 * @since 1.17
 */
public enum Command
{
  LOG, BROWSE, CAT, DIFF, BLAME,

  /**
   * @since 1.18
   */
  TAGS,

  /**
   * @since 1.18
   */
  BRANCHES,

  /**
   * @since 1.31
   */
  INCOMING, OUTGOING, PUSH, PULL,

  /**
   * @since 1.43
   */
  BUNDLE, UNBUNDLE,

  /**
   * @since 2.0
   */
  MODIFICATIONS, MERGE, DIFF_RESULT, BRANCH, MODIFY,

  /**
   * @since 2.10.0
   */
  LOOKUP,

  /**
   * @since 2.11.0
   */
  TAG,

  /**
   * @since 2.17.0
   */
  FULL_HEALTH_CHECK,

  /**
   * @since 2.19.0
   */
  MIRROR,

  /**
   * @since 2.26.0
   */
  FILE_LOCK,

  /**
   * @since 2.28.0
   */
  BRANCH_DETAILS,
  /**
   * @since 2.39.0
   */
  CHANGESETS
}
