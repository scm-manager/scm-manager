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

import java.io.Serializable;

/**
 *
 * @since 1.17
 */
public final class LogCommandRequest implements Serializable, Resetable
{

  private static final long serialVersionUID = 8759598040394428649L;

  private String branch;

  private String endChangeset;

  private int pagingLimit = 20;

  private int pagingStart = 0;

  private String path;

  private String startChangeset;

  private String ancestorChangeset;

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

    //J-
    return Objects.equal(startChangeset, other.startChangeset)
      && Objects.equal(endChangeset, other.endChangeset)
      && Objects.equal(pagingStart, other.pagingStart)
      && Objects.equal(pagingLimit, other.pagingLimit)
      && Objects.equal(path, other.path) 
      && Objects.equal(branch, other.branch)
      && Objects.equal(ancestorChangeset, other.ancestorChangeset);
    //J+
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(startChangeset, endChangeset, pagingStart,
      pagingLimit, path, branch, ancestorChangeset);
  }

   @Override
  public void reset()
  {
    startChangeset = null;
    endChangeset = null;
    pagingStart = 0;
    pagingLimit = 20;
    path = null;
    branch = null;
    ancestorChangeset = null;
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("startChangeset", startChangeset)
                  .add("endChangeset", endChangeset)
                  .add("pagingStart", pagingStart)
                  .add("pagingLimit", pagingLimit)
                  .add("path", path)
                  .add("branch", branch)
                  .add("ancestorChangeset", ancestorChangeset)
                  .toString();
    //J+
  }



  public void setBranch(String branch)
  {
    this.branch = branch;
  }


  public void setEndChangeset(String endChangeset)
  {
    this.endChangeset = endChangeset;
  }


  public void setPagingLimit(int pagingLimit)
  {
    this.pagingLimit = pagingLimit;
  }


  public void setPagingStart(int pagingStart)
  {
    this.pagingStart = pagingStart;
  }


  public void setPath(String path)
  {
    this.path = path;
  }


  public void setStartChangeset(String startChangeset)
  {
    this.startChangeset = startChangeset;
  }

  public void setAncestorChangeset(String ancestorChangeset) {
    this.ancestorChangeset = ancestorChangeset;
  }


  
  public String getBranch()
  {
    return branch;
  }

  
  public String getEndChangeset()
  {
    return endChangeset;
  }

  
  public int getPagingLimit()
  {
    return pagingLimit;
  }

  
  public int getPagingStart()
  {
    return pagingStart;
  }

  
  public String getPath()
  {
    return path;
  }

  
  public String getStartChangeset()
  {
    return startChangeset;
  }

  
  public boolean isPagingUnlimited()
  {
    return pagingLimit < 0;
  }

  public String getAncestorChangeset() {
    return ancestorChangeset;
  }

}
