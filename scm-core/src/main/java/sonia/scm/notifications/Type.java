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

package sonia.scm.notifications;

/**
 * Type of notification.
 * @since 2.18.0
 */
public enum Type {
  /**
   * Notifications with an informative character e.g.: update available
   */
  INFO,

  /**
   * Success should be used if a long running action is finished successfully e.g.: export is ready to download
   */
  SUCCESS,

  /**
   * Notifications with a warning character e.g.: disk space is filled up to 80 percent.
   */
  WARNING,

  /**
   * Error should be used in the case of an failure e.g.: export failed
   */
  ERROR
}
