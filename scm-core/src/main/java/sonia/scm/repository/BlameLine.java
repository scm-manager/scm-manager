/**
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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * Single line of a file, in a {@link  BlameResult}.
 *
 * @author Sebastian Sdorra
 * @since 1.8
 */
public class BlameLine implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = 2816601606921153670L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public BlameLine() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param author
   * @param when
   * @param revision
   * @param description
   * @param code
   * @param lineNumber
   */
  public BlameLine(int lineNumber, String revision, Long when, Person author,
                   String description, String code)
  {
    this.lineNumber = lineNumber;
    this.revision = revision;
    this.when = when;
    this.author = author;
    this.description = description;
    this.code = code;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
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

    final BlameLine other = (BlameLine) obj;

    return Objects.equal(lineNumber, other.lineNumber)
           && Objects.equal(revision, other.revision)
           && Objects.equal(author, other.author)
           && Objects.equal(when, other.when)
           && Objects.equal(code, other.code)
           && Objects.equal(description, other.description);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(lineNumber, revision, author, when, code,
                            description);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("lineNumber", lineNumber)
            .add("revision", revision)
            .add("author", author)
            .add("when", when)
            .add("code", code)
            .add("description", description)
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
  public Person getAuthor()
  {
    return author;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getCode()
  {
    return code;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getLineNumber()
  {
    return lineNumber;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRevision()
  {
    return revision;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getWhen()
  {
    return when;
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
   * @param code
   */
  public void setCode(String code)
  {
    this.code = code;
  }

  /**
   * Method description
   *
   *
   * @param description
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Method description
   *
   *
   * @param lineNumber
   */
  public void setLineNumber(int lineNumber)
  {
    this.lineNumber = lineNumber;
  }

  /**
   * Method description
   *
   *
   * @param revision
   */
  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  /**
   * Method description
   *
   *
   * @param when
   */
  public void setWhen(Long when)
  {
    this.when = when;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Person author;

  /** Field description */
  private String code;

  /** Field description */
  private String description;

  /** Field description */
  private int lineNumber;

  /** Field description */
  private String revision;

  /** Field description */
  private Long when;
}
