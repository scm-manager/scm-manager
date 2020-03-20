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

import com.google.common.base.Objects;
import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Represents a changeset/commit of a repository.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "changeset")
@XmlAccessorType(XmlAccessType.FIELD)
public class Changeset extends BasicPropertiesAware implements ModelObject {

  private static final long serialVersionUID = -8373308448928993039L;

  /**
   * The author of the changeset
   */
  private Person author;

  /**
   * The name of the branches on which the changeset was committed.
   */
  private List<String> branches;

  /**
   * The date when the changeset was committed
   */
  private Long date;

  /**
   * The text of the changeset description
   */
  private String description;

  /**
   * The changeset identification string
   */
  private String id;

  /**
   * parent changeset ids
   */
  private List<String> parents;

  /**
   * The tags associated with the changeset
   */
  private List<String> tags;

  public Changeset() {}

  public Changeset(String id, Long date, Person author)
  {
    this(id, date, author, null);
  }

  public Changeset(String id, Long date, Person author, String description)
  {
    this.id = id;
    this.date = date;
    this.author = author;
    this.description = description;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass()) {
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
                            branches,  properties);
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

    return out.toString();
  }


  /**
   * Returns a timestamp of the creation date of the {@link Changeset}.
   *
   * @return a timestamp of the creation date of the {@link Changeset}
   */
  public Long getCreationDate() {
    return getDate();
  }

  @Override
  public void setCreationDate(Long timestamp) {
    this.setDate(timestamp);
  }


  /**
   * Returns the author of the changeset.
   *
   *
   * @return author of the changeset
   */
  public Person getAuthor() {
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
  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setLastModified(Long timestamp) {
    throw new UnsupportedOperationException("changesets are immutable");
  }

  @Override
  public Long getLastModified() {
    return null;
  }

  @Override
  public String getType() {
    return "Changeset";
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

}
