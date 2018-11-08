package sonia.scm.repository.api;

/**
 * This class keeps the result of a merge dry run. Use {@link #isMergeable()} to check whether an automatic merge is
 * possible or not.
 */
public class MergeDryRunCommandResult {

  private final boolean mergeable;

  public MergeDryRunCommandResult(boolean mergeable) {
    this.mergeable = mergeable;
  }

  /**
   * This will return <code>true</code>, when an automatic merge is possible <em>at the moment</em>; <code>false</code>
   * otherwise.
   */
  public boolean isMergeable() {
    return mergeable;
  }
}
