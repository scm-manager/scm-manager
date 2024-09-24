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
