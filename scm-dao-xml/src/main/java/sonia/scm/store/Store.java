/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
