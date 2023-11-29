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
    
package sonia.scm.api.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

//~--- JDK imports ------------------------------------------------------------

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
    return MoreObjects.toStringHelper(this)
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
