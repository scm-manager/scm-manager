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

import java.io.File;

enum Store {
  CONFIG("config"),
  DATA("data"),
  BLOB("blob");

  private static final String GLOBAL_STORE_BASE_DIRECTORY = "var";
  static final String STORE_DIRECTORY = "store";

  static Store forStoreType(StoreType storeType) {
    switch (storeType) {
      case BLOB:
        return BLOB;
      case DATA:
        return DATA;
      case CONFIG:
      case CONFIG_ENTRY:
        return CONFIG;
      default:
        throw new IllegalArgumentException("unknown store type: " + storeType);
    }
  }

  private final String directory;

  Store(String directory) {

    this.directory = directory;
  }

  /**
   * Get the relative store directory path to be stored in the repository root
   * <p>
   * The repository store directories are:
   * repo_base_dir/store/config/
   * repo_base_dir/store/blob/
   * repo_base_dir/store/data/
   *
   * @return the relative store directory path to be stored in the repository root
   */
  public String getRepositoryStoreDirectory() {
    return STORE_DIRECTORY + File.separator + directory;
  }

  /**
   * Get the relative store directory path to be stored in the namespace root
   * <p>
   * The namespace store directories are:
   * namespace_dir/store/config/
   * namespace_dir/store/blob/
   * namespace_dir/store/data/
   *
   * @return the relative store directory path to be stored in the namespace root
   */
  public String getNamespaceStoreDirectory() {
    return STORE_DIRECTORY + File.separator + directory;
  }

  /**
   * Get the relative store directory path to be stored in the global root
   * <p>
   * The global store directories are:
   * base_dir/config/
   * base_dir/var/blob/
   * base_dir/var/data/
   *
   * @return the relative store directory path to be stored in the global root
   */
  public String getGlobalStoreDirectory() {
    if (this.equals(CONFIG)) {
      return directory;
    }
    return GLOBAL_STORE_BASE_DIRECTORY + File.separator + directory;
  }
}
