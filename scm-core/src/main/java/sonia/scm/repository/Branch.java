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

package sonia.scm.repository;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.Validateable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a branch in a repository.
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
@XmlRootElement(name = "branch")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Branch implements Serializable, Validateable {

  private static final String VALID_CHARACTERS_AT_START_AND_END = "\\w-,;\\]{}@&+=$#`|<>";
  private static final String VALID_CHARACTERS = VALID_CHARACTERS_AT_START_AND_END + "/.";
  public static final String VALID_BRANCH_NAMES = "[" + VALID_CHARACTERS_AT_START_AND_END + "]([" + VALID_CHARACTERS + "]*[" + VALID_CHARACTERS_AT_START_AND_END + "])?";
  public static final Pattern VALID_BRANCH_NAME_PATTERN = Pattern.compile(VALID_BRANCH_NAMES);

  private static final long serialVersionUID = -4602244691711222413L;

  private String name;

  private String revision;

  private boolean defaultBranch;

  private Long lastCommitDate;

  private boolean stale = false;

  /**
   * Constructs a new instance of branch.
   * This constructor should only be called from JAXB.
   */
  Branch() {}

  /**
   * Constructs a new branch.
   *
   * @param name name of the branch
   * @param revision latest revision of the branch
   * @param defaultBranch Whether this branch is the default branch for the repository
   *
   * @deprecated Use {@link Branch#Branch(String, String, boolean, Long)} instead.
   */
  @Deprecated
  Branch(String name, String revision, boolean defaultBranch) {
    this(name, revision, defaultBranch, null);
  }

  /**
   * Constructs a new branch.
   *
   * @param name name of the branch
   * @param revision latest revision of the branch
   * @param defaultBranch Whether this branch is the default branch for the repository
   * @param lastCommitDate The date of the commit this branch points to (if computed). May be <code>null</code>
   */
  Branch(String name, String revision, boolean defaultBranch, Long lastCommitDate) {
    this.name = name;
    this.revision = revision;
    this.defaultBranch = defaultBranch;
    this.lastCommitDate = lastCommitDate;
  }

  /**
   * @deprecated Use {@link #normalBranch(String, String, Long)} instead to set the date of the last commit, too.
   */
  @Deprecated
  public static Branch normalBranch(String name, String revision) {
    return normalBranch(name, revision, null);
  }

  public static Branch normalBranch(String name, String revision, Long lastCommitDate) {
    return new Branch(name, revision, false, lastCommitDate);
  }

  /**
   * @deprecated Use {@link #defaultBranch(String, String, Long)} instead to set the date of the last commit, too.
   */
  @Deprecated
  public static Branch defaultBranch(String name, String revision) {
    return defaultBranch(name, revision, null);
  }

  public static Branch defaultBranch(String name, String revision, Long lastCommitDate) {
    return new Branch(name, revision, true, lastCommitDate);
  }

  public void setStale(boolean stale) {
    this.stale = stale;
  }

  @Override
  public boolean isValid() {
    return VALID_BRANCH_NAME_PATTERN.matcher(name).matches();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Branch other = (Branch) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(revision, other.revision)
      && Objects.equal(defaultBranch, other.defaultBranch)
      && Objects.equal(lastCommitDate, other.lastCommitDate);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, revision);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                  .add("name", name)
                  .add("revision", revision)
                  .add("defaultBranch", defaultBranch)
                  .add("lastCommitDate", lastCommitDate)
                  .toString();
  }

  /**
   * Returns the name of the branch
   *
   * @return name of the branch
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the latest revision of the branch.
   *
   * @return latest revision of branch
   */
  public String getRevision() {
    return revision;
  }

  /**
   * Flag whether this branch is configured as the default branch.
   */
  public boolean isDefaultBranch() {
    return defaultBranch;
  }

  /**
   * The date of the commit this branch points to, if this was computed (can be empty).
   *
   * @since 2.11.0
   */
  public Optional<Long> getLastCommitDate() {
    return Optional.ofNullable(lastCommitDate);
  }

  public boolean isStale() {
    return stale;
  }
}
