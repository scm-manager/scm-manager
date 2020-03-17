/**
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
