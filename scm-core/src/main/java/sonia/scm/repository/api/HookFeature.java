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
 * Enumeration of available hook features.
 *
 * @since 1.33
 */
public enum HookFeature
{

  /**
   * Hook message provider
   */
  MESSAGE_PROVIDER,

  /**
   * Hook changeset provider
   */
  CHANGESET_PROVIDER,

  /**
   * Hook branch provider
   *
   * @since 1.45
   */
  BRANCH_PROVIDER,

  /**
   * Hook tag provider
   *
   * @since 1.50
   */
  TAG_PROVIDER,

  /**
   * Provider to detect merges
   *
   * @since 2.4.0
   */
  MERGE_DETECTION_PROVIDER,

  /**
   * Provider to compute modifications
   *
   * @since 2.48.0
   */
  MODIFICATIONS_PROVIDER
}
