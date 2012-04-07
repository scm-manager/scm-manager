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


package sonia.scm.api.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "exception")
public class RestExceptionResult
{

  /**
   * Constructs ...
   *
   */
  public RestExceptionResult() {}

  /**
   * Constructs ...
   *
   *
   * @param throwable
   */
  public RestExceptionResult(Throwable throwable)
  {
    this(throwable.getMessage(), throwable);
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   * @param stacktrace
   */
  public RestExceptionResult(String message, String stacktrace)
  {
    this.message = message;
    this.stacktrace = stacktrace;
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   * @param throwable
   */
  public RestExceptionResult(String message, Throwable throwable)
  {
    this(message, Throwables.getStackTraceAsString(throwable));
  }

  //~--- methods --------------------------------------------------------------

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

    final RestExceptionResult other = (RestExceptionResult) obj;

    return Objects.equal(message, other.message)
           && Objects.equal(stacktrace, other.stacktrace);
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
    return Objects.hashCode(message, stacktrace);
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
            .add("message", message)
            .add("stacktrace", stacktrace)
            .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getStacktrace()
  {
    return stacktrace;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param message
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  /**
   * Method description
   *
   *
   * @param stacktrace
   */
  public void setStacktrace(String stacktrace)
  {
    this.stacktrace = stacktrace;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String message;

  /** Field description */
  private String stacktrace;
}
