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

package sonia.scm.store;

/**
 * Store constants for xml implementations.
 *
 */
public class StoreConstants
{

  private StoreConstants() {  }

  public static final String CONFIG_DIRECTORY_NAME = "config";

  /**
   * Name of the parent of data or blob directories.
   * @since 2.23.0
   */
  public static final String VARIABLE_DATA_DIRECTORY_NAME = "var";

  /**
   * Name of data directories.
   * @since 2.23.0
   */
  public static final String DATA_DIRECTORY_NAME = "data";

  /**
   * Name of blob directories.
   * @since 2.23.0
   */
  public static final String BLOG_DIRECTORY_NAME = "data";

  public static final String REPOSITORY_METADATA = "metadata";

  public static final String FILE_EXTENSION = ".xml";
}
