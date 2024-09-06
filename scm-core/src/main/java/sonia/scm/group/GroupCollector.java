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

package sonia.scm.group;

import java.util.Set;

public interface GroupCollector {

  String AUTHENTICATED = "_authenticated";

  Set<String> collect(String principal);

  /**
   * Returns the groups of the user that had been assigned at the last login (including all
   * external groups) and the current internal groups associated to the user. If the
   * user had not logged in before, only the current internal groups will be returned.
   *
   * @since 2.42.0
   */
  Set<String> fromLastLoginPlusInternal(String principal);
}
