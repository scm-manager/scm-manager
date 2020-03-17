/**
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
