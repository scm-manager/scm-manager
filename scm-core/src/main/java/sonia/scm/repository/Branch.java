/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.Validateable;

import java.io.Serializable;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a branch in a repository.
 *
 * @since 1.18
 */
@XmlRootElement(name = "branch")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Branch implements Serializable, Validateable {

  /*
  The regex for branches is based on the rules for git branch names taken
  from the reference format check (https://git-scm.com/docs/git-check-ref-format)

  Below you find the rules, taken from the site. Rules 3, 8 and 9 are not implemented,
  because they cannot simply be checked using a regular expression.

  1. They can include slash / for hierarchical (directory) grouping, but no slash-separated component can begin with a dot . or end with the sequence .lock.
  2. [not relevant for branches]
  3. They cannot have two consecutive dots .. anywhere.
  4. They cannot have ASCII control characters (i.e. bytes whose values are lower than \040, or \177 DEL), space, tilde ~, caret ^, or colon : anywhere.
  5. They cannot have question-mark ?, asterisk *, or open bracket [ anywhere. See the --refspec-pattern option below for an exception to this rule.
  6. They cannot begin or end with a slash / or contain multiple consecutive slashes (see the --normalize option below for an exception to this rule)
  7. They cannot end with a dot ..
  8. They cannot contain a sequence @{.
  9. They cannot be the single character @.
  10. They cannot contain a \.
   */

  private static final String ILLEGAL_CHARACTERS = "\\\\/\\s\\[~^:?*";
  private static final String VALID_PATH_PART = "[^." + ILLEGAL_CHARACTERS + "](?:[^" + ILLEGAL_CHARACTERS + "]*[^." + ILLEGAL_CHARACTERS + "])?";
  public static final String VALID_BRANCH_NAMES = VALID_PATH_PART + "(?:/" + VALID_PATH_PART + ")*";
  public static final Pattern VALID_BRANCH_NAME_PATTERN = Pattern.compile(VALID_BRANCH_NAMES);

  private static final long serialVersionUID = -4602244691711222413L;

  private String name;

  private String revision;

  private boolean defaultBranch;

  private Long lastCommitDate;
  private Person lastCommitter;

  private boolean stale = false;

  /**
   * Constructs a new instance of branch.
   * This constructor should only be called from JAXB.
   */
  Branch() {
  }

  /**
   * Constructs a new branch.
   *
   * @param name          name of the branch
   * @param revision      latest revision of the branch
   * @param defaultBranch Whether this branch is the default branch for the repository
   * @deprecated Use {@link Branch#Branch(String, String, boolean, Long, Person)} instead.
   */
  @Deprecated
  Branch(String name, String revision, boolean defaultBranch) {
    this(name, revision, defaultBranch, null);
  }

  /**
   * Constructs a new branch.
   *
   * @param name           name of the branch
   * @param revision       latest revision of the branch
   * @param defaultBranch  Whether this branch is the default branch for the repository
   * @param lastCommitDate The date of the commit this branch points to (if computed). May be <code>null</code>
   * @deprecated Use {@link Branch#Branch(String, String, boolean, Long, Person)} instead.
   */
  @Deprecated
  Branch(String name, String revision, boolean defaultBranch, Long lastCommitDate) {
    this(name, revision, defaultBranch, lastCommitDate, null);
  }

  /**
   * Constructs a new branch.
   *
   * @param name           name of the branch
   * @param revision       latest revision of the branch
   * @param defaultBranch  Whether this branch is the default branch for the repository
   * @param lastCommitDate The date of the commit this branch points to (if computed). May be <code>null</code>
   * @param lastCommitter  The user of the commit this branch points to (if computed). May be <code>null</code>
   */
  Branch(String name, String revision, boolean defaultBranch, Long lastCommitDate, Person lastCommitter) {
    this.name = name;
    this.revision = revision;
    this.defaultBranch = defaultBranch;
    this.lastCommitDate = lastCommitDate;
    this.lastCommitter = lastCommitter;
  }

  /**
   * @deprecated Use {@link #normalBranch(String, String, Long, Person)} instead to set the date of the last commit, too.
   */
  @Deprecated
  public static Branch normalBranch(String name, String revision) {
    return normalBranch(name, revision, null);
  }

  /**
   * @deprecated Use {@link #normalBranch(String, String, Long, Person)} instead to set the author of the last commit, too.
   */
  @Deprecated
  public static Branch normalBranch(String name, String revision, Long lastCommitDate) {
    return normalBranch(name, revision, lastCommitDate, null);
  }

  public static Branch normalBranch(String name, String revision, Long lastCommitDate, Person lastCommitter) {
    return new Branch(name, revision, false, lastCommitDate, lastCommitter);
  }

  /**
   * @deprecated Use {@link #defaultBranch(String, String, Long)} instead to set the date of the last commit, too.
   */
  @Deprecated
  public static Branch defaultBranch(String name, String revision) {
    return defaultBranch(name, revision, null);
  }

  /**
   * @deprecated Use {@link #defaultBranch(String, String, Long, Person)} instead to set the author of the last commit, too.
   */
  @Deprecated
  public static Branch defaultBranch(String name, String revision, Long lastCommitDate) {
    return defaultBranch(name, revision, lastCommitDate, null);
  }

  public static Branch defaultBranch(String name, String revision, Long lastCommitDate, Person lastCommitter) {
    return new Branch(name, revision, true, lastCommitDate, lastCommitter);
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
      && Objects.equal(lastCommitDate, other.lastCommitDate)
      && Objects.equal(lastCommitter, other.lastCommitter);
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
      .add("lastCommitter", lastCommitter)
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


  /**
   * The author of the last commit this branch points to.
   *
   * @since 2.28.0
   */
  public Person getLastCommitter() {
    return lastCommitter;
  }

  public boolean isStale() {
    return stale;
  }
}
