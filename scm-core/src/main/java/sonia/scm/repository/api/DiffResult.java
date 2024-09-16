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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
   *
   * @since 3.4.0
   */
  default Optional<DiffStatistics> getStatistics() {
    return empty();
  }

  /**
   * This function returns all file paths wrapped in a tree
   *
   * @since 3.5.0
   */
  default Optional<DiffTreeNode> getDiffTree() {
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

  @Value
  class DiffTreeNode {

    String nodeName;
    Map<String, DiffTreeNode> children = new LinkedHashMap<>();
    Optional<DiffFile.ChangeType> changeType;

    public Map<String, DiffTreeNode> getChildren() {
      return Collections.unmodifiableMap(children);
    }

    public static DiffTreeNode createRootNode() {
      return new DiffTreeNode("", Optional.empty());
    }

    private DiffTreeNode(String nodeName, Optional<DiffFile.ChangeType> changeType) {
      this.nodeName = nodeName;
      this.changeType = changeType;
    }

    public void addChild(String path, DiffFile.ChangeType changeType) {
      traverseAndAddChild(path.split("/"), 0, changeType);
    }

    private void traverseAndAddChild(String[] pathSegments, int index, DiffFile.ChangeType changeType) {
      if (index == pathSegments.length) {
        return;
      }

      String currentPathSegment = pathSegments[index];
      DiffTreeNode child = children.get(currentPathSegment);

      if (child == null) {
        boolean isFilename = index == pathSegments.length - 1;
        child = new DiffTreeNode(currentPathSegment, isFilename ? Optional.of(changeType) : Optional.empty());
        children.put(currentPathSegment, child);
      }

      child.traverseAndAddChild(pathSegments, index + 1, changeType);
    }
  }
}
