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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
/**
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public abstract class PagedRemoteCommandRequest extends RemoteCommandRequest
{

  /**
   * {@inheritDoc}
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

    final PagedRemoteCommandRequest other = (PagedRemoteCommandRequest) obj;

    return Objects.equal(remoteRepository, other.remoteRepository)
      && Objects.equal(pagingStart, other.pagingStart)
      && Objects.equal(pagingLimit, other.pagingLimit);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(remoteRepository, pagingStart, pagingLimit);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {

    //J-
    return MoreObjects.toStringHelper(this)
                  .add("remoteURL", remoteRepository)
                  .add("pagingStart", pagingStart)
                  .add("pagingLimit", pagingLimit)
                  .toString();
    //J+
  }

  //~--- set methods ----------------------------------------------------------

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

  //~--- get methods ----------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private int pagingLimit = 20;

  /** Field description */
  private int pagingStart = 0;
}
