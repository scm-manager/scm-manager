package sonia.scm.repository.api;

import java.util.Collection;
import java.util.HashSet;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

/**
 * This class keeps the result of a merge of branches. Use {@link #isSuccess()} to check whether the merge was
 * sucessfully executed. If the result is <code>false</code> the merge could not be done without conflicts. In this
 * case you can use {@link #getFilesWithConflict()} to get a list of files with merge conflicts.
 */
public class MergeCommandResult {
  private final Collection<String> filesWithConflict;

  private MergeCommandResult(Collection<String> filesWithConflict) {
    this.filesWithConflict = filesWithConflict;
  }

  public static MergeCommandResult success() {
    return new MergeCommandResult(emptyList());
  }

  public static MergeCommandResult failure(Collection<String> filesWithConflict) {
    return new MergeCommandResult(new HashSet<>(filesWithConflict));
  }

  /**
   * If this returns <code>true</code>, the merge was successfull. If this returns <code>false</code> there were
   * merge conflicts. In this case you can use {@link #getFilesWithConflict()} to check what files could not be merged.
   */
  public boolean isSuccess() {
    return filesWithConflict.isEmpty();
  }

  /**
   * If the merge was not successful ({@link #isSuccess()} returns <code>false</code>) this will give you a list of
   * file paths that could not be merged automatically.
   */
  public Collection<String> getFilesWithConflict() {
    return unmodifiableCollection(filesWithConflict);
  }
}
