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
    
package sonia.scm.repository.client.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.repository.Person;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
public final class CommitRequest
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

    final CommitRequest other = (CommitRequest) obj;

    return Objects.equal(author, other.author)
      && Objects.equal(message, other.message);
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
    return Objects.hashCode(author, message);
  }

  /**
   * Method description
   *
   */
  public void reset()
  {
    this.author = null;
    this.message = null;
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
                  .add("author", author)
                  .add("message", message)
                  .toString();
    //J+
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param author
   */
  public void setAuthor(Person author)
  {
    this.author = author;
  }

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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  Person getAuthor()
  {
    return author;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  String getMessage()
  {
    return message;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Person author;

  /** Field description */
  private String message;
}
