package sonia.scm.repository.spi;

import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.MergeStrategy;

import java.util.Set;

public interface MergeCommand {
  MergeCommandResult merge(MergeCommandRequest request);

  MergeDryRunCommandResult dryRun(MergeCommandRequest request);

  MergeConflictResult computeConflicts(MergeCommandRequest request);

  boolean isSupported(MergeStrategy strategy);

  Set<MergeStrategy> getSupportedMergeStrategies();
}
