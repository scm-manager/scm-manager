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
    
package sonia.scm.repository;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * Single line of a file, in a {@link  BlameResult}.
 *
 * @since 1.8
 */
public class BlameLine implements Serializable
{

  private static final long serialVersionUID = 2816601606921153670L;

  private Person author;

  private String code;

  private String description;

  private int lineNumber;

  private String revision;

  private Long when;

  public BlameLine() {}

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

  @Override
  public int hashCode()
  {
    return Objects.hashCode(lineNumber, revision, author, when, code,
                            description);
  }

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



  public Person getAuthor()
  {
    return author;
  }


  public String getCode()
  {
    return code;
  }


  public String getDescription()
  {
    return description;
  }


  public int getLineNumber()
  {
    return lineNumber;
  }


  public String getRevision()
  {
    return revision;
  }


  public Long getWhen()
  {
    return when;
  }



  public void setAuthor(Person author)
  {
    this.author = author;
  }


  public void setCode(String code)
  {
    this.code = code;
  }


  public void setDescription(String description)
  {
    this.description = description;
  }


  public void setLineNumber(int lineNumber)
  {
    this.lineNumber = lineNumber;
  }


  public void setRevision(String revision)
  {
    this.revision = revision;
  }


  public void setWhen(Long when)
  {
    this.when = when;
  }

}
