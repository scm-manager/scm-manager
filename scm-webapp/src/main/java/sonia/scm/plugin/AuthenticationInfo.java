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

package sonia.scm.plugin;

import java.time.Instant;

/**
 * Information about the plugin center authentication.
 * @since 2.28.0
 */
public interface AuthenticationInfo {

  /**
   * Returns the username of the SCM-Manager user which has authenticated the plugin center.
   * @return SCM-Manager username
   */
  String getPrincipal();

  /**
   * Returns the subject of the plugin center user.
   * @return plugin center subject
   */
  String getPluginCenterSubject();

  /**
   * Returns the date on which the authentication was performed.
   * @return authentication date
   */
  Instant getDate();

  /**
   * Returns {@code true} if the last authentication has failed.
   * @return {@code true} if the last authentication has failed.
   * @since 2.31.0
   */
  default boolean isFailed() {
    return false;
  }

}
