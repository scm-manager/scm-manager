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

package sonia.scm.repository.api;

import lombok.Value;

import java.util.Optional;

import static java.util.Optional.empty;

public interface DiffResult extends Iterable<DiffFile> {

  String getOldRevision();

  String getNewRevision();

  default boolean isPartial() {
    return false;
  }

  default int getOffset() {
    return 0;
  }

  default Optional<Integer> getLimit() {
    return empty();
  }

  default IgnoreWhitespaceLevel getIgnoreWhitespace() {
    return IgnoreWhitespaceLevel.NONE;
  }

  /**
   * This function returns statistics if they are supported.
   * @since 3.4.0
   */
  default Optional<DiffStatistics> getStatistics() {
    return empty();
  }

  @Value
  class DiffStatistics {
    /**
     * number of added files in a diff
     */
    int added;
    /**
     * number of modified files in a diff
     */
    int modified;
    /**
     * number of deleted files in a diff
     */
    int deleted;
  }

}
