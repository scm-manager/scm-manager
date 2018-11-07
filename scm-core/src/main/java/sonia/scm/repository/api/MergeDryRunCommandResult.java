package sonia.scm.repository.api;

public class MergeDryRunCommandResult {

  private final boolean mergeable;

  public MergeDryRunCommandResult(boolean mergeable) {
    this.mergeable = mergeable;
  }

  public boolean isMergeable() {
    return mergeable;
  }
}
