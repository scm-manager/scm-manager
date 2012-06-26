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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

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
    return Objects.toStringHelper(this)
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
