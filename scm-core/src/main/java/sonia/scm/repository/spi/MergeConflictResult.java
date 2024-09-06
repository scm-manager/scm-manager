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

package sonia.scm.repository.spi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.ADDED_BY_BOTH;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.BOTH_MODIFIED;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.DELETED_BY_THEM;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.DELETED_BY_US;

public class MergeConflictResult {

  private final List<SingleMergeConflict> conflicts = new LinkedList<>();

  public List<SingleMergeConflict> getConflicts() {
    return Collections.unmodifiableList(conflicts);
  }

  public void addBothModified(String path, String diff) {
    conflicts.add(new SingleMergeConflict(BOTH_MODIFIED, path, diff));
  }

  public void addDeletedByThem(String path) {
    conflicts.add(new SingleMergeConflict(DELETED_BY_THEM, path, null));
  }

  public void addDeletedByUs(String path) {
    conflicts.add(new SingleMergeConflict(DELETED_BY_US, path, null));
  }

  public void addAddedByBoth(String path) {
    conflicts.add(new SingleMergeConflict(ADDED_BY_BOTH, path, null));
  }

  public static class SingleMergeConflict {
    private final ConflictTypes type;
    private final String path;
    private final String diff;

    private SingleMergeConflict(ConflictTypes type, String path, String diff) {
      this.type = type;
      this.path = path;
      this.diff = diff;
    }

    public ConflictTypes getType() {
      return type;
    }

    public String getPath() {
      return path;
    }

    public String getDiff() {
      return diff;
    }
  }

  public enum ConflictTypes {
    BOTH_MODIFIED, DELETED_BY_THEM, DELETED_BY_US, ADDED_BY_BOTH
  }
}
