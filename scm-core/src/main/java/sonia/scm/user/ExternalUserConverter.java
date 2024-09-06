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

package sonia.scm.user;

import sonia.scm.plugin.ExtensionPoint;

/**
 * The external user converter can be used to modify users
 * which are provided by external systems before creation in SCM-Manager.
 * The implementations will be called in the {@link sonia.scm.security.SyncingRealmHelper}
 * @since 2.9.0
 */
@ExtensionPoint
public interface ExternalUserConverter {

  /**
   * Returns the converted user.
   * @return converted user
   */
  User convert(User user);
}
