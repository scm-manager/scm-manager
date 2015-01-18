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

import sonia.scm.BasicPropertiesAware;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a changeset/commit of a repository.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "changeset")
@XmlAccessorType(XmlAccessType.FIELD)
public class Changeset extends BasicPropertiesAware
        implements Validateable, Serializable
{

  /** Field description */
  private static final long serialVersionUID = -8373308448928993039L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance of changeset.
   *
   */
  public Changeset() {}

  /**
   * Constructs a new instance of changeset.
   *
   *
   * @param id id of the changeset
   * @param date date of the changeset
   * @param author author of the changeset
   */
  public Changeset(String id, Long date, Person author)
  {
    this(id, date, author, null);
  }

  /**
   * Constructs a new instance of changeset.
   *
   *
   * @param id id of the changeset
   * @param date date of the changeset
   * @param author author of the changeset
   * @param description description of the changeset
   */
  public Changeset(String id, Long date, Person author, String description)
  {
    this.id = id;
    this.date = date;
    this.author = author;
    this.description = description;
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

    final Changeset other = (Changeset) obj;

    //J-
    return Objects.equal(id, other.id) 
           && Objects.equal(date, other.date)
           && Objects.equal(author, other.author)
           && Objects.equal(description, other.description)
           && Objects.equal(parents, other.parents)
           && Objects.equal(tags, other.tags)
           && Objects.equal(branches, other.branches)
           && Objects.equal(modifications, other.modifications)
           && Objects.equal(properties, other.properties);
    //J+
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
    return Objects.hashCode(id, date, author, description, parents, tags,
                            branches, modifications, properties);
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
    StringBuilder out = new StringBuilder("changeset: ");

    out.append(id).append("\n");

    if (parents != null)
    {
      out.append("parents: ").append(Util.toString(parents)).append("\n");
    }

    out.append("author: ").append(author).append("\n");

    if (date != null)
    {
      out.append("date: ").append(Util.formatDate(new Date(date))).append("\n");
    }

    out.append("desc:").append(description).append("\n");
    out.append("branches: ").append(Util.toString(branches)).append("\n");
    out.append("tags: ").append(Util.toString(tags)).append("\n");

    if (modifications != null)
    {
      out.append("modifications: \n").append(modifications);
    }

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the author of the changeset.
   *
   *
   * @return author of the changeset
   */
  public Person getAuthor()
  {
    return author;
  }

  /**
   * Returns the branches of the changeset. In the most cases a changeset is 
   * only related to one branch, but in the case of receive hooks it is possible 
   * that a changeset is related to more than a branch.
   *
   *
   * @return branches of the changeset
   */
  public List<String> getBranches()
  {
    if (branches == null)
    {
      branches = new ArrayList<String>();
    }

    return branches;
  }

  /**
   * Returns the creation date of the changeset.
   *
   *
   * @return date of the changeset
   */
  public Long getDate()
  {
    return date;
  }

  /**
   * Return the description (commit message) of the changeset.
   *
   *
   * @return description of the changeset
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the id of the changeset.
   *
   *
   * @return id of the changeset
   */
  public String getId()
  {
    return id;
  }

  /**
   * Returns the file modifications, which was done with this changeset.
   *
   *
   * @return file modifications
   */
  public Modifications getModifications()
  {
    if (modifications == null)
    {
      modifications = new Modifications();
    }

    return modifications;
  }

  /**
   * Return the ids of the parent changesets.
   *
   *
   * @return
   * @since 1.11
   */
  public List<String> getParents()
  {
    if (parents == null)
    {
      parents = new ArrayList<String>();
    }

    return parents;
  }

  /**
   * Returns tags associated with this changeset.
   *
   *
   * @return tags of the changeset
   */
  public List<String> getTags()
  {
    if (tags == null)
    {
      tags = new ArrayList<String>();
    }

    return tags;
  }

  /**
   * Returns true if the changeset is valid.
   *
   *
   * @return true if the changeset is valid
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(id) && ValidationUtil.isValid(author)
           && (date != null);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the author of the changeset.
   *
   *
   * @param author author of the changeset
   */
  public void setAuthor(Person author)
  {
    this.author = author;
  }

  /**
   * Sets the branches of the changeset.
   *
   *
   * @param branches branches of the changeset
   */
  public void setBranches(List<String> branches)
  {
    this.branches = branches;
  }

  /**
   * Sets the date of the changeset.
   *
   *
   * @param date date of the changeset
   */
  public void setDate(Long date)
  {
    this.date = date;
  }

  /**
   * Sets the description (commit message) of the changeset.
   *
   *
   * @param description description of the changeset
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Sets the id of the changeset.
   *
   *
   * @param id id of the changeset
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Sets the file modification of the changeset.
   *
   *
   * @param modifications file modifications
   */
  public void setModifications(Modifications modifications)
  {
    this.modifications = modifications;
  }

  /**
   * Sets the parents of the changeset.
   *
   *
   * @param parents parents of the changeset
   * @since 1.11
   */
  public void setParents(List<String> parents)
  {
    this.parents = parents;
  }

  /**
   * Sets the tags of the changeset
   *
   *
   * @param tags tags of the changeset
   */
  public void setTags(List<String> tags)
  {
    this.tags = tags;
  }

  //~--- fields ---------------------------------------------------------------

  /** The author of the changeset */
  private Person author;

  /** The name of the branches on which the changeset was committed. */
  private List<String> branches;

  /** The date when the changeset was committed */
  private Long date;

  /** The text of the changeset description */
  private String description;

  /** The changeset identification string */
  private String id;

  /** List of files changed by this changeset */
  @XmlElement(name = "modifications")
  private Modifications modifications;

  /** parent changeset ids */
  private List<String> parents;

  /** The tags associated with the changeset */
  private List<String> tags;
}
