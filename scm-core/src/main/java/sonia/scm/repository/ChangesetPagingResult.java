/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.*;
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the current list of changesets.
   *
   *
   * @param changesets current list of changesets
   */
  public void setChangesets(List<Changeset> changesets)
  {
    this.changesets = changesets;
  }

  /**
   * Sets the total number of changesets
   *
   *
   * @param total total number of changesets
   */
  public void setTotal(int total)
  {
    this.total = total;
  }

  //~--- fields ---------------------------------------------------------------

  /** current list of changesets */
  @XmlElement(name = "changeset")
  @XmlElementWrapper(name = "changesets")
  private List<Changeset> changesets;

  /** total number of changesets */
  private int total;
}
