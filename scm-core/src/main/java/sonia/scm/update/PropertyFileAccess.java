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

package sonia.scm.update;

import java.io.IOException;
import java.nio.file.Path;

public interface PropertyFileAccess {

  /**
   * Use this to rename a configuration file.
   * @param oldName The old file name.
   * @return Object to specify the new file name.
   */
  Target renameGlobalConfigurationFrom(String oldName);

  interface Target {
    /**
     * Renames a file to the new name given here.
     * @param newName The new file name.
     * @throws IOException If the file could not be renamed.
     */
    void to(String newName) throws IOException;
  }

  /**
   * Creates a tool object for store migration from v1 to v2.
   * @param name The name of the store to be handled.
   * @return The tool object for the named store.
   */
  StoreFileTools forStoreName(String name);

  interface StoreFileTools {
    /**
     * Iterates over all found store files (that is, files ending with ".xml") and calls the
     * given consumer for each file, giving the file and the name of the data file.
     * @param storeFileConsumer This consumer will be called for each file found.
     * @throws IOException May be thrown when an exception occurs.
     */
    void forStoreFiles(FileConsumer storeFileConsumer) throws IOException;

    /**
     * Moves a data file to the new location for a repository with the given id. If there
     * is no directory for the given repository id, nothing will be done.
     * @param storeFile The name of the store file.
     * @param repositoryId The id of the repository as the new target for the store file.
     * @throws IOException When the file could not be moved.
     */
    void moveAsRepositoryStore(Path storeFile, String repositoryId) throws IOException;
  }

  interface FileConsumer {
    void accept(Path file, String storeName) throws IOException;
  }
}
