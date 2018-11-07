package sonia.scm.repository.api;

import java.util.Collection;
import java.util.HashSet;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

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

  public boolean isSuccess() {
    return filesWithConflict.isEmpty();
  }

  public Collection<String> getFilesWithConflict() {
    return unmodifiableCollection(filesWithConflict);
  }
}
