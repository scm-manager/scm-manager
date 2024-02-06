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

package sonia.scm.repository;

import com.google.common.io.ByteSource;
import sonia.scm.plugin.ExtensionPoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Use this {@link RepositoryContentInitializer} to create new files with custom content
 * which will be included in the initial commit of the new repository
 */
@ExtensionPoint
public interface RepositoryContentInitializer {

  /**
   * @param context add content to this context in order to commit files in the initial repository commit
   */
  void initialize(InitializerContext context) throws IOException;

  /**
   * Use this {@link InitializerContext} to create new files on repository initialization
   * which will be included in the first commit
   */
  interface InitializerContext {

    /**
     * @return repository to which this initializerContext belongs to
     */
    Repository getRepository();

    /**
     * create new file which will be included in initial repository commit
     */
    CreateFile create(String path);

    /**
     * Returns the context entry with the given key and unmarshalls it to the given type.
     * It no entry with the given key is available an empty optional is returned.
     *
     * @param key  key of the context object
     * @param type type of the context object
     * @return context entry or empty optional
     * @since 2.5.0
     */
    default <T> Optional<T> getEntry(String key, Class<T> type) {
      return Optional.empty();
    }
  }

  /**
   * Use this to apply content to new files which should be committed on repository initialization
   */
  interface CreateFile {

    /**
     * Applies content to new file
     *
     * @param content content of file as string
     * @return {@link InitializerContext}
     * @throws IOException
     */
    InitializerContext from(String content) throws IOException;

    /**
     * Applies content to new file
     *
     * @param input content of file as input stream
     * @return {@link InitializerContext}
     * @throws IOException
     */
    InitializerContext from(InputStream input) throws IOException;

    /**
     * Applies content to new file
     *
     * @param byteSource content of file as byte source
     * @return {@link InitializerContext}
     * @throws IOException
     */
    InitializerContext from(ByteSource byteSource) throws IOException;

  }
}
