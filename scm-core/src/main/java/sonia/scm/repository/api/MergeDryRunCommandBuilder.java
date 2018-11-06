package sonia.scm.repository.api;

import com.google.common.base.Preconditions;
import sonia.scm.repository.spi.MergeCommand;
import sonia.scm.repository.spi.MergeCommandRequest;

public class MergeDryRunCommandBuilder {

  private final MergeCommand mergeCommand;
  private final MergeCommandRequest request = new MergeCommandRequest();

  public MergeDryRunCommandBuilder(MergeCommand mergeCommand) {
    this.mergeCommand = mergeCommand;
  }

  public MergeDryRunCommandBuilder setBranchToMerge(String branchToMerge) {
    request.setBranchToMerge(branchToMerge);
    return this;
  }

  public MergeDryRunCommandBuilder setTargetBranch(String targetBranch) {
    request.setTargetBranch(targetBranch);
    return this;
  }

  public MergeDryRunCommandBuilder reset() {
    request.reset();
    return this;
  }

  public boolean execute() {
    Preconditions.checkArgument(request.isValid(), "revision to merge and target revision is required");
    return mergeCommand.merge(request);
  }
}
