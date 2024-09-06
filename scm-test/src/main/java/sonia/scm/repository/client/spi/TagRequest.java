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

package sonia.scm.repository.client.spi;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
/**
 *
 * @since 1.18
 */
public final class TagRequest
{
  private String name;

  private String revision;

  private String username;

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

    final TagRequest other = (TagRequest) obj;

    return Objects.equal(revision, other.revision)
      && Objects.equal(name, other.name);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(revision, name);
  }

   public void reset()
  {
    this.name = null;
    this.revision = null;
    this.username = null;
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("revision", revision)
                  .add("name", name)
      .add("username", username)
                  .toString();
    //J+
  }



  public void setName(String name)
  {
    this.name = name;
  }


  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  public void setUsername(String username) {
    this.username = username;
  }


  
  String getName()
  {
    return name;
  }

  
  String getRevision()
  {
    return revision;
  }

  public String getUserName() {
    return username;
  }

}
