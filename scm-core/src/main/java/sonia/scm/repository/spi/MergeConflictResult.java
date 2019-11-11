package sonia.scm.repository.spi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.BOTH_MODIFIED;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.DELETED_BY_THEM;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.DELETED_BY_US;

public class MergeConflictResult {

  private final List<SingleMergeConflict> conflicts = new LinkedList<>();

  public List<SingleMergeConflict> getConflicts() {
    return Collections.unmodifiableList(conflicts);
  }

  void addBothModified(String path, String diff) {
    conflicts.add(new SingleMergeConflict(BOTH_MODIFIED, path, diff));
  }

  void addDeletedByThem(String path) {
    conflicts.add(new SingleMergeConflict(DELETED_BY_THEM, path, null));
  }

  void addDeletedByUs(String path) {
    conflicts.add(new SingleMergeConflict(DELETED_BY_US, path, null));
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
    BOTH_MODIFIED, DELETED_BY_THEM, DELETED_BY_US
  }
}
