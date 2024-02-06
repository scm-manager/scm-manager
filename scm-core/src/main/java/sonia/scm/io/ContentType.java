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
import java.util.Optional;

/**
 * Detected type of content.
 *
 * @since 2.23.0
 */
public interface ContentType {

  /**
   * Returns the primary part of the content type (e.g.: text of text/plain).
   */
  String getPrimary();

  /**
   * Returns the secondary part of the content type (e.g.: plain of text/plain).
   */
  String getSecondary();

  /**
   * Returns the raw presentation of the content type (e.g.: text/plain).
   */
  String getRaw();

  /**
   * Returns {@code true} if the content type is text based.
   */
  boolean isText();

  /**
   * Returns an optional with the programming language
   * or empty if the content is not programming language.
   */
  Optional<String> getLanguage();

  /**
   * Returns a map of syntax modes such as codemirror, ace or prism.
   * @since 2.28.0
   */
  default Map<String, String> getSyntaxModes() {
    return Collections.emptyMap();
  }
}
