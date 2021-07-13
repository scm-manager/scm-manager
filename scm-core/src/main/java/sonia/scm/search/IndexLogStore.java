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

package sonia.scm.search;

import java.util.Optional;

/**
 * Can be used to mark when a type of object was last indexed and with which version.
 * This is useful to detect and mark if a bootstrap index was created for the kind of object
 * or if the way how an object is indexed has changed.
 *
 * @since 2.21.0
 */
public interface IndexLogStore {

  /**
   * Log index and version of a type which is now indexed.
   *
   * @param index name of index
   * @param type type which was indexed
   * @param version model version
   */
  void log(String index, Class<?> type, int version);

  /**
   * Returns version and date of the indexed type or an empty object,
   * if the object was not indexed at all.
   *
   * @param index name if index
   * @param type type of object
   *
   * @return log entry or empty
   */
  Optional<IndexLog> get(String index, Class<?> type);

}
