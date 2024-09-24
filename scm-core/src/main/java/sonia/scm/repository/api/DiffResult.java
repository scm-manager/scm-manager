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
    /**
     * number of renamed files in a diff
     */
    int renamed;
    /**
     * number of copy files in a diff
     */
    int copied;
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
