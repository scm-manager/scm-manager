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

package sonia.scm.io;

import java.util.Collections;
import java.util.Map;

/**
 * ContentTypeResolver is able to detect the {@link ContentType} of files based on their path and (optinally) a few starting bytes. These files do not have to be real files on the file system, but can be hypothetical constructs ("What content type is most probable for a file named like this").
 *
 * @since 2.23.0
 */
public interface ContentTypeResolver {

  /**
   * Detects the {@link ContentType} of the given path, by only using path based strategies.
   *
   * @param path path of the file
   * @return {@link ContentType} of path
   */
  ContentType resolve(String path);

  /**
   * Detects the {@link ContentType} of the given path, by using path and content based strategies.
   *
   * @param path          path of the file
   * @param contentPrefix first few bytes of the content
   * @return {@link ContentType} of path and content prefix
   */
  ContentType resolve(String path, byte[] contentPrefix);

  /**
   * Returns a map of syntax highlighting modes such as ace, codemirror or prism by language.
   * @param language name of the coding language
   * @since 2.28.0
   */
  default Map<String, String> findSyntaxModesByLanguage(String language) {
    return Collections.emptyMap();
  }
}
