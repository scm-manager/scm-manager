package sonia.scm.repository.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import sonia.scm.Validateable;

import java.io.Serializable;

public class MergeDryRunCommandRequest implements Validateable, Resetable, Serializable, Cloneable {

  private static final long serialVersionUID = -2650236557922431528L;

  private String branchToMerge;
  private String targetBranch;

  public String getBranchToMerge() {
    return branchToMerge;
  }

  public void setBranchToMerge(String branchToMerge) {
    this.branchToMerge = branchToMerge;
  }

  public String getTargetBranch() {
    return targetBranch;
  }

  public void setTargetBranch(String targetBranch) {
    this.targetBranch = targetBranch;
  }

  public boolean isValid() {
    return !Strings.isNullOrEmpty(getBranchToMerge()) && !Strings.isNullOrEmpty(getTargetBranch());
  }

  public void reset() {
    this.setBranchToMerge(null);
    this.setTargetBranch(null);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final MergeDryRunCommandRequest other = (MergeDryRunCommandRequest) obj;

    return Objects.equal(branchToMerge, other.branchToMerge)
      && Objects.equal(targetBranch, other.targetBranch);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(branchToMerge, targetBranch);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("branchToMerge", branchToMerge)
      .add("targetBranch", targetBranch)
      .toString();
  }
}
