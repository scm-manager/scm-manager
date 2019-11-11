package sonia.scm.repository.spi;

import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;

public interface MergeCommand {
  MergeCommandResult merge(MergeCommandRequest request);

  MergeDryRunCommandResult dryRun(MergeCommandRequest request);

  MergeConflictResult computeConflicts(MergeCommandRequest request);
}
