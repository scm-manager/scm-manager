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
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * Represents all branches of a repository.
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
@XmlRootElement(name="branches")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Branches implements Iterable<Branch>
{

  /**
   * Constructs a new instance of branches.
   * This constructor should only be called from JAXB.
   *
   */
  public Branches() {}

  /**
   * Constructs a new instance of branches.
   *
   *
   * @param branches list of branches.
   */
  public Branches(Branch... branches)
  {
    this.branches = Lists.newArrayList(branches);
  }

  /**
   * Constructs a new instance of branches.
   *
   *
   * @param branches list of branches.
   */
  public Branches(List<Branch> branches)
  {
    this.branches = branches;
  }

  //~--- methods --------------------------------------------------------------

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

    final Branches other = (Branches) obj;

    return Objects.equal(branches, other.branches);
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
    return Objects.hashCode(branches);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<Branch> iterator()
  {
    return getBranches().iterator();
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
                  .add("branches", branches)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns all branches of a repository.
   *
   *
   * @return all branches
   */
  public List<Branch> getBranches()
  {
    if (branches == null)
    {
      branches = Lists.newArrayList();
    }

    return branches;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets all branches.
   *
   *
   * @param branches branches
   */
  public void setBranches(List<Branch> branches)
  {
    this.branches = branches;
  }

  //~--- fields ---------------------------------------------------------------

  /** branches */
  @XmlElement(name="branch")
  private List<Branch> branches;
}
