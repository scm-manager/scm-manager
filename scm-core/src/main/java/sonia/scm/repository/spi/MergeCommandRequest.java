package sonia.scm.repository.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import sonia.scm.Validateable;
import sonia.scm.repository.Person;
import sonia.scm.util.Util;

import java.io.Serializable;

public class MergeCommandRequest implements Validateable, Resetable, Serializable, Cloneable {

  private static final long serialVersionUID = -2650236557922431528L;

  private String branchToMerge;
  private String targetBranch;
  private Person author;

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

  public Person getAuthor() {
    return author;
  }

  public void setAuthor(Person author) {
    this.author = author;
  }

  public boolean isValid() {
    return !Strings.isNullOrEmpty(getBranchToMerge())
      && !Strings.isNullOrEmpty(getTargetBranch())
      && getAuthor() != null;
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

    final MergeCommandRequest other = (MergeCommandRequest) obj;

    return Objects.equal(branchToMerge, other.branchToMerge)
      && Objects.equal(targetBranch, other.targetBranch)
      && Objects.equal(author, other.author);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(branchToMerge, targetBranch, author);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("branchToMerge", branchToMerge)
      .add("targetBranch", targetBranch)
      .add("author", author)
      .toString();
  }
}
