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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.Validateable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.regex.Pattern;

//~--- JDK imports ------------------------------------------------------------

/**
 * Represents a branch in a repository.
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
@XmlRootElement(name = "branch")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Branch implements Serializable, Validateable
{

  private static final String VALID_CHARACTERS_AT_START_AND_END = "\\w-,;\\]{}@&+=$#`|<>";
  private static final String VALID_CHARACTERS = VALID_CHARACTERS_AT_START_AND_END + "/.";
  public static final String VALID_BRANCH_NAMES = "[" + VALID_CHARACTERS_AT_START_AND_END + "]([" + VALID_CHARACTERS + "]*[" + VALID_CHARACTERS_AT_START_AND_END + "])?";
  public static final Pattern VALID_BRANCH_NAME_PATTERN = Pattern.compile(VALID_BRANCH_NAMES);

  /** Field description */
  private static final long serialVersionUID = -4602244691711222413L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance of branch.
   * This constructor should only be called from JAXB.
   *
   */
  Branch() {}

  /**
   * Constructs a new branch.
   *
   *
   * @param name name of the branch
   * @param revision latest revision of the branch
   */
  Branch(String name, String revision, boolean defaultBranch)
  {
    this.name = name;
    this.revision = revision;
    this.defaultBranch = defaultBranch;
  }

  public static Branch normalBranch(String name, String revision) {
    return new Branch(name, revision, false);
  }

  public static Branch defaultBranch(String name, String revision) {
    return new Branch(name, revision, true);
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public boolean isValid() {
    return VALID_BRANCH_NAME_PATTERN.matcher(name).matches();
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final Branch other = (Branch) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(revision, other.revision)
      && Objects.equal(defaultBranch, other.defaultBranch);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, revision);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("name", name)
                  .add("revision", revision)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the name of the branch
   *
   *
   * @return name of the branch
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the latest revision of the branch.
   *
   * @return latest revision of branch
   */
  public String getRevision()
  {
    return revision;
  }

  public boolean isDefaultBranch() {
    return defaultBranch;
  }

  //~--- fields ---------------------------------------------------------------

  /** name of the branch */
  private String name;

  /** Field description */
  private String revision;

  private boolean defaultBranch;
}
