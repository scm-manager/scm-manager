/**
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * The changeset paging result is used to do a paging over the
 * {@link Changeset}s of a {@link Repository}.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "changeset-paging")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChangesetPagingResult implements Iterable<Changeset>, Serializable
{

  /** Field description */
  private static final long serialVersionUID = -8678755403658841733L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new changeset paging result.
   *
   */
  public ChangesetPagingResult() {}

  /**
   * Constructs a new changeset paging result.
   *
   *
   * @param total total number of changesets
   * @param changesets current list of fetched changesets
   */
  public ChangesetPagingResult(int total, List<Changeset> changesets)
  {
    this.total = total;
    this.changesets = changesets;
    this.branchName = null;
  }

  /**
   * Constructs a new changeset paging result for a specific branch.
   *
   *
   * @param total total number of changesets
   * @param changesets current list of fetched changesets
   * @param branchName branch name this result was created for
   */
  public ChangesetPagingResult(int total, List<Changeset> changesets, String branchName)
  {
    this.total = total;
    this.changesets = changesets;
    this.branchName = branchName;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
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

    final ChangesetPagingResult other = (ChangesetPagingResult) obj;

    return Objects.equal(changesets, other.changesets)
      && Objects.equal(total, other.total);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(changesets, total);
  }

  /**
   * Returns an iterator which can iterate over the current list of changesets.
   *
   *
   * @return iterator for current list of changesets
   * @since 1.8
   */
  @Override
  public Iterator<Changeset> iterator()
  {
    Iterator<Changeset> it = null;

    if (changesets != null)
    {
      it = changesets.iterator();
    }

    return it;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("changesets", changesets)
                  .add("total", total)
                  .add("branch", branchName)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the current list of changesets.
   *
   *
   * @return current list of changesets
   */
  public List<Changeset> getChangesets()
  {
    return changesets;
  }

  /**
   * Returns the total number of changesets.
   *
   *
   * @return total number of changesets
   */
  public int getTotal()
  {
    return total;
  }

  void setChangesets(List<Changeset> changesets)
  {
    this.changesets = changesets;
  }

  void setTotal(int total)
  {
    this.total = total;
  }

  void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  /**
   * Returns the branch name this result was created for. This can either be an explicit branch ("give me all
   * changesets for branch xyz") or an implicit one ("give me the changesets for the default").
   */
  public String getBranchName() {
    return branchName;
  }

  //~--- fields ---------------------------------------------------------------

  @XmlElement(name = "changeset")
  @XmlElementWrapper(name = "changesets")
  private List<Changeset> changesets;

  private int total;

  private String branchName;
}
