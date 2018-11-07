package sonia.scm.repository.api;

public class MergeCommandResult {
  private final boolean success;

  public MergeCommandResult(boolean success) {
    this.success = success;
  }

  public boolean isSuccess() {
    return success;
  }
}
