package sonia.scm.repository.api;

public class BranchRequest {
  private String parentBranch;
  private String newBranch;

  public String getParentBranch() {
    return parentBranch;
  }

  public void setParentBranch(String parentBranch) {
    this.parentBranch = parentBranch;
  }

  public String getNewBranch() {
    return newBranch;
  }

  public void setNewBranch(String newBranch) {
    this.newBranch = newBranch;
  }
}
