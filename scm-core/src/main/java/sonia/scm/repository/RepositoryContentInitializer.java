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
     * create new file which will be included in initial repository commit.
     *
     * @param path full path of the file to be created
     * @param useDefaultPath Wether the default path of the repository should be prefixed to the specified path. For example "/trunk", if it is a SVN repository.
     */
    CreateFile createWithDefaultPath(String path, boolean useDefaultPath);

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
