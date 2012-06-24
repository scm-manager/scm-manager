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

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "changeset-paging")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChangesetPagingResult implements Iterable<Changeset>, Cloneable
{

  /**
   * Constructs ...
   *
   */
  public ChangesetPagingResult() {}

  /**
   * Constructs ...
   *
   *
   * @param total
   * @param changesets
   */
  public ChangesetPagingResult(int total, List<Changeset> changesets)
  {
    this.total = total;
    this.changesets = changesets;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Create a clone of this {@link ChangesetPagingResult} object.
   *
   *
   * @return clone of this {@link ChangesetPagingResult}
   *
   * @since 1.17
   */
  @Override
  public ChangesetPagingResult clone()
  {
    ChangesetPagingResult changesetPagingResult = null;

    try
    {
      changesetPagingResult = (ChangesetPagingResult) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      throw new RuntimeException(ex);
    }

    return changesetPagingResult;
  }

  /**
   * Method description
   *
   *
   * @return
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<Changeset> getChangesets()
  {
    return changesets;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getTotal()
  {
    return total;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param changesets
   */
  public void setChangesets(List<Changeset> changesets)
  {
    this.changesets = changesets;
  }

  /**
   * Method description
   *
   *
   * @param total
   */
  public void setTotal(int total)
  {
    this.total = total;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "changeset")
  @XmlElementWrapper(name = "changesets")
  private List<Changeset> changesets;

  /** Field description */
  private int total;
}
