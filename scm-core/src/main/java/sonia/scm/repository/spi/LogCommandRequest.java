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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class LogCommandRequest implements Serializable, Resetable
{

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

    final LogCommandRequest other = (LogCommandRequest) obj;

    return Objects.equal(startChangeset, other.startChangeset)
           && Objects.equal(endChangeset, other.endChangeset)
           && Objects.equal(pagingStart, other.pagingStart)
           && Objects.equal(pagingLimit, other.pagingLimit)
           && Objects.equal(branch, other.branch)
           && Objects.equal(path, other.path)
           && Objects.equal(pending, other.pending);
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
    return Objects.hashCode(startChangeset, endChangeset, pagingStart,
                            pagingLimit, branch, path, pending);
  }

  /**
   * Method description
   *
   */
  @Override
  public void reset()
  {
    startChangeset = null;
    endChangeset = null;
    pagingStart = 0;
    pagingLimit = 20;
    path = null;
    branch = null;
    pending = false;
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
    return Objects.toStringHelper(this)
                  .add("startChangeset", startChangeset)
                  .add("endChangeset", endChangeset)
                  .add("pagingStart", pagingStart)
                  .add("pagingLimit", pagingLimit)
                  .add("branch", branch)
                  .add("path", path)
                  .add("pending", pending)
                  .toString();
    //J+
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param branch
   */
  public void setBranch(String branch)
  {
    this.branch = branch;
  }

  /**
   * Method description
   *
   *
   * @param endChangeset
   */
  public void setEndChangeset(String endChangeset)
  {
    this.endChangeset = endChangeset;
  }

  /**
   * Method description
   *
   *
   * @param pagingLimit
   */
  public void setPagingLimit(int pagingLimit)
  {
    this.pagingLimit = pagingLimit;
  }

  /**
   * Method description
   *
   *
   * @param pagingStart
   */
  public void setPagingStart(int pagingStart)
  {
    this.pagingStart = pagingStart;
  }

  /**
   * Method description
   *
   *
   * @param path
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  /**
   * Method description
   *
   *
   * @param pending
   */
  public void setPending(boolean pending)
  {
    this.pending = pending;
  }

  /**
   * Method description
   *
   *
   * @param startChangeset
   */
  public void setStartChangeset(String startChangeset)
  {
    this.startChangeset = startChangeset;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  String getBranch()
  {
    return branch;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  String getEndChangeset()
  {
    return endChangeset;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  int getPagingLimit()
  {
    return pagingLimit;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  int getPagingStart()
  {
    return pagingStart;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  String getPath()
  {
    return path;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  String getStartChangeset()
  {
    return startChangeset;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  boolean isPagingUnlimited()
  {
    return pagingLimit < 0;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  boolean isPending()
  {
    return pending;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String branch;

  /** Field description */
  private String endChangeset;

  /** Field description */
  private int pagingLimit = 20;

  /** Field description */
  private int pagingStart = 0;

  /** Field description */
  private String path;

  /** Field description */
  private boolean pending = false;

  /** Field description */
  private String startChangeset;
}
