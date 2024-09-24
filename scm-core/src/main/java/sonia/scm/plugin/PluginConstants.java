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

/**
 *
 * @since 2.0.0
 */
public interface PluginConstants
{

  /** checksum file */
  public static final String FILE_CHECKSUM = "checksum";

  /** core file */
  public static final String FILE_CORE = "core";

  public static final String ID_DELIMITER = ":";

  public static final String PATH_DESCRIPTOR = "/META-INF/scm/plugin.xml";

  public static final String FILE_DESCRIPTOR = PATH_DESCRIPTOR.substring(1);
}
