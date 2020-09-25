/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import sonia.scm.Validateable;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeStrategy;
import sonia.scm.repository.util.AuthorUtil.CommandWithAuthor;

import java.io.Serializable;

public class MergeCommandRequest implements Validateable, Resetable, Serializable, Cloneable, CommandWithAuthor {

  private static final long serialVersionUID = -2650236557922431528L;

  private String branchToMerge;
  private String targetBranch;
  private Person author;
  private String messageTemplate;
  private String message;
  private MergeStrategy mergeStrategy;
  private boolean sign = true;

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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public MergeStrategy getMergeStrategy() {
    return mergeStrategy;
  }

  public void setMergeStrategy(MergeStrategy mergeStrategy) {
    this.mergeStrategy = mergeStrategy;
  }

  public boolean isSign() {
    return sign;
  }

  public void setSign(boolean sign) {
    this.sign = sign;
  }

  public boolean isValid() {
    return !Strings.isNullOrEmpty(getBranchToMerge())
      && !Strings.isNullOrEmpty(getTargetBranch());
  }

  public void reset() {
    this.setBranchToMerge(null);
    this.setTargetBranch(null);
    this.setSign(true);
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
      && Objects.equal(mergeStrategy, other.mergeStrategy)
      && Objects.equal(sign, other.sign);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(branchToMerge, targetBranch, author, mergeStrategy);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("branchToMerge", branchToMerge)
      .add("targetBranch", targetBranch)
      .add("author", author)
      .add("mergeStrategy", mergeStrategy)
      .add("sign", sign)
      .toString();
  }
}
