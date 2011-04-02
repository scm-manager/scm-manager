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

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.xml.XmlTimestampDateAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name="changeset")
public class Changeset implements Validateable, Cloneable, Serializable
{

  /** Field description */
  private static final long serialVersionUID = -8373308448928993039L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Changeset() {}

  /**
   * Constructs ...
   *
   *
   * @param id
   * @param date
   * @param author
   */
  public Changeset(String id, Long date, String author)
  {
    this(id, date, author, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param id
   * @param date
   * @param author
   * @param description
   */
  public Changeset(String id, Long date, String author, String description)
  {
    this.id = id;
    this.date = date;
    this.author = author;
    this.description = description;
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

    final Changeset other = (Changeset) obj;

    if ((this.author == null)
        ? (other.author != null)
        : !this.author.equals(other.author))
    {
      return false;
    }

    if ((this.date != other.date)
        && ((this.date == null) ||!this.date.equals(other.date)))
    {
      return false;
    }

    if ((this.description == null)
        ? (other.description != null)
        : !this.description.equals(other.description))
    {
      return false;
    }

    if ((this.id == null)
        ? (other.id != null)
        : !this.id.equals(other.id))
    {
      return false;
    }

    return true;
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
    int hash = 7;

    hash = 71 * hash + ((this.author != null)
                        ? this.author.hashCode()
                        : 0);
    hash = 71 * hash + ((this.date != null)
                        ? this.date.hashCode()
                        : 0);
    hash = 71 * hash + ((this.description != null)
                        ? this.description.hashCode()
                        : 0);
    hash = 71 * hash + ((this.id != null)
                        ? this.id.hashCode()
                        : 0);

    return hash;
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
    StringBuilder out = new StringBuilder("changeset: ");

    out.append(id).append("\n");
    out.append("author: ").append(author).append("\n");

    if (date != null)
    {
      out.append("date: ").append(Util.formatDate(new Date(date))).append("\n");
    }

    out.append("desc:").append(description).append("\n");

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAuthor()
  {
    return author;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getDate()
  {
    return date;
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
  public String getId()
  {
    return id;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(id) && Util.isNotEmpty(author) && (date != null);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param author
   */
  public void setAuthor(String author)
  {
    this.author = author;
  }

  /**
   * Method description
   *
   *
   * @param date
   */
  public void setDate(Long date)
  {
    this.date = date;
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
   * @param id
   */
  public void setId(String id)
  {
    this.id = id;
  }

  //~--- fields ---------------------------------------------------------------

  /** The author of the changeset */
  private String author;

  /** The date when the changeset was committed */
  @XmlJavaTypeAdapter(XmlTimestampDateAdapter.class)
  private Long date;

  /** The text of the changeset description */
  private String description;

  /** The changeset identification string */
  private String id;
}
