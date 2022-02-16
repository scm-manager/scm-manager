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
   * @return map of syntax highlighting modes
   * @since 2.28.0
   */
  default Map<String, String> findSyntaxModesByLanguage(String language) {
    return Collections.emptyMap();
  }
}
