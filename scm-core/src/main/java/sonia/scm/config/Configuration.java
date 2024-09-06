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

package sonia.scm.config;

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;

/**
 * Base for all kinds of configurations.
 *
 * Allows for permission like
 *
 * <ul>
 *   <li>"configuration:read:global",</li>
 *   <li>"configuration:write:svn",</li>
 *   <li>"configuration:*:git",</li>
 *   <li>"configuration:*"</li>
 * </ul>
 *
 * <br/>
 *
 * And for permission checks like {@code ConfigurationPermissions.read(configurationObject).check();}
 */
@StaticPermissions(
  value = "configuration",
  permissions = {"read", "write"},
  globalPermissions = {"list"},
  custom = true, customGlobal = true
)
public interface Configuration extends PermissionObject {
}
