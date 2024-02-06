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
/**
 *
 * @since 1.31
 */
public abstract class PagedRemoteCommandRequest extends RemoteCommandRequest
{
  private int pagingLimit = 20;

  private int pagingStart = 0;
 
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

 
  @Override
  public int hashCode()
  {
    return Objects.hashCode(remoteRepository, pagingStart, pagingLimit);
  }

 
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



  public void setPagingLimit(int pagingLimit)
  {
    this.pagingLimit = pagingLimit;
  }


  public void setPagingStart(int pagingStart)
  {
    this.pagingStart = pagingStart;
  }


  
  int getPagingLimit()
  {
    return pagingLimit;
  }

  
  int getPagingStart()
  {
    return pagingStart;
  }

}
