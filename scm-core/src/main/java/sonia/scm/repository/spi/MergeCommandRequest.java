package sonia.scm.repository.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import sonia.scm.Validateable;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.ScmMergeStrategy;
import sonia.scm.repository.util.AuthorUtil.CommandWithAuthor;

import java.io.Serializable;

public class MergeCommandRequest implements Validateable, Resetable, Serializable, Cloneable, CommandWithAuthor {

  private static final long serialVersionUID = -2650236557922431528L;

  private String branchToMerge;
  private String targetBranch;
  private Person author;
  private String messageTemplate;
  private ScmMergeStrategy scmMergeStrategy;

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

  public String getMessageTemplate() {
    return messageTemplate;
  }

  public void setMessageTemplate(String messageTemplate) {
    this.messageTemplate = messageTemplate;
  }

  public ScmMergeStrategy getScmMergeStrategy() {
    return scmMergeStrategy;
  }

  public void setScmMergeStrategy(ScmMergeStrategy scmMergeStrategy) {
    this.scmMergeStrategy = scmMergeStrategy;
  }

  public boolean isValid() {
    return !Strings.isNullOrEmpty(getBranchToMerge())
      && !Strings.isNullOrEmpty(getTargetBranch());
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
      && Objects.equal(author, other.author)
      && Objects.equal(scmMergeStrategy, other.scmMergeStrategy);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(branchToMerge, targetBranch, author, scmMergeStrategy);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("branchToMerge", branchToMerge)
      .add("targetBranch", targetBranch)
      .add("author", author)
      .add("mergeStrategy", scmMergeStrategy)
      .toString();
  }
}
