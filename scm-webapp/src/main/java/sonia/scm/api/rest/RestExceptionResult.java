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

package sonia.scm.api.rest;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "exception")
public class RestExceptionResult
{
  private String message;

  private String stacktrace;

  public RestExceptionResult() {}

 
  public RestExceptionResult(Throwable throwable)
  {
    this(throwable.getMessage(), throwable);
  }

 
  public RestExceptionResult(String message, String stacktrace)
  {
    this.message = message;
    this.stacktrace = stacktrace;
  }


  public RestExceptionResult(String message, Throwable throwable)
  {
    this(message, Throwables.getStackTraceAsString(throwable));
  }



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

    final RestExceptionResult other = (RestExceptionResult) obj;

    return Objects.equal(message, other.message)
           && Objects.equal(stacktrace, other.stacktrace);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(message, stacktrace);
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("message", message)
            .add("stacktrace", stacktrace)
            .toString();
    //J+
  }


  
  public String getMessage()
  {
    return message;
  }

  
  public String getStacktrace()
  {
    return stacktrace;
  }



  public void setMessage(String message)
  {
    this.message = message;
  }


  public void setStacktrace(String stacktrace)
  {
    this.stacktrace = stacktrace;
  }

}
