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
import sonia.scm.repository.Person;

/**
 *
 * @since 1.18
 */
public final class CommitRequest
{
  private Person author;

  private String message;

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

    final CommitRequest other = (CommitRequest) obj;

    return Objects.equal(author, other.author)
      && Objects.equal(message, other.message);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(author, message);
  }

   public void reset()
  {
    this.author = null;
    this.message = null;
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("author", author)
                  .add("message", message)
                  .toString();
    //J+
  }



  public void setAuthor(Person author)
  {
    this.author = author;
  }


  public void setMessage(String message)
  {
    this.message = message;
  }


  
  Person getAuthor()
  {
    return author;
  }

  
  String getMessage()
  {
    return message;
  }

}
