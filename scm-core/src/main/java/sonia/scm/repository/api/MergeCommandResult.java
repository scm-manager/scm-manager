package sonia.scm.repository.api;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

/**
 * This class keeps the result of a merge of branches. Use {@link #isSuccess()} to check whether the merge was
 * sucessfully executed. If the result is <code>false</code> the merge could not be done without conflicts. In this
 * case you can use {@link #getFilesWithConflict()} to get a list of files with merge conflicts.
 */
public class MergeCommandResult {
  private final Collection<String> filesWithConflict;
  private String newHeadRevision;

  public MergeCommandResult(Collection<String> filesWithConflict) {
    this.filesWithConflict = filesWithConflict;
  }

  public MergeCommandResult(Collection<String> filesWithConflict, String newHeadRevision) {
    this.filesWithConflict = filesWithConflict;
    this.newHeadRevision = newHeadRevision;
  }

  /**
   * If this returns <code>true</code>, the merge was successfull. If this returns <code>false</code> there were
   * merge conflicts. In this case you can use {@link #getFilesWithConflict()} to check what files could not be merged.
   */
  public boolean isSuccess() {
    return filesWithConflict.isEmpty() && newHeadRevision != null;
  }

  /**
   * If the merge was not successful ({@link #isSuccess()} returns <code>false</code>) this will give you a list of
   * file paths that could not be merged automatically.
   */
  public Collection<String> getFilesWithConflict() {
    return unmodifiableCollection(filesWithConflict);
  }

  public String getNewHeadRevision() {
    return newHeadRevision;
  }
}
