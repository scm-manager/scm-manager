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

package sonia.scm.repository;

import lombok.EqualsAndHashCode;
import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Represents a changeset/commit of a repository.
 *
 */
@EqualsAndHashCode(callSuper = true)
public class Changeset extends BasicPropertiesAware implements ModelObject {

  private static final long serialVersionUID = -8373308448928993039L;

  private Person author;

  /**
   * The name of the branches on which the changeset was committed.
   */
  private List<String> branches;

  /**
   * The date when the changeset was committed
   */
  private Long date;

  private String description;

  private String id;

  /**
   * parent changeset ids
   */
  private List<String> parents;

  /**
   * The tags associated with the changeset
   */
  private List<String> tags;

  /**
   * Trailers for this changeset like reviewers or co-authors
   */
  private Collection<Contributor> contributors;

  private List<Signature> signatures = new ArrayList<>();

  public Changeset() {}

  public Changeset(String id, Long date, Person author)
  {
    this(id, date, author, null);
  }

  public Changeset(String id, Long date, Person author, String description) {
    this.id = id;
    this.date = date;
    this.author = author;
    this.description = description;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("changeset: ");

    out.append(id).append("\n");

    if (parents != null) {
      out.append("parents: ").append(Util.toString(parents)).append("\n");
    }

    out.append("author: ").append(author).append("\n");

    if (date != null) {
      out.append("date: ").append(Util.formatDate(new Date(date))).append("\n");
    }

    out.append("desc:").append(description).append("\n");
    out.append("branches: ").append(Util.toString(branches)).append("\n");
    out.append("tags: ").append(Util.toString(tags)).append("\n");

    return out.toString();
  }

  /**
   * Returns a timestamp of the creation date of the {@link Changeset}.
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
   */
  public Person getAuthor() {
    return author;
  }

  /**
   * Returns the branches of the changeset. In the most cases a changeset is
   * only related to one branch, but in the case of receive hooks it is possible
   * that a changeset is related to more than a branch.
   *
   * @return branches of the changeset
   */
  public List<String> getBranches() {
    if (branches == null) {
      branches = new ArrayList<>();
    }

    return branches;
  }

  /**
   * Returns the creation date of the changeset.
   */
  public Long getDate() {
    return date;
  }

  /**
   * Return the description (commit message) of the changeset.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the id of the changeset.
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
   * @return
   * @since 1.11
   */
  public List<String> getParents() {
    if (parents == null) {
      parents = new ArrayList<>();
    }

    return parents;
  }

  /**
   * Returns tags associated with this changeset.
   *
   * @return tags of the changeset
   */
  public List<String> getTags() {
    if (tags == null) {
      tags = new ArrayList<>();
    }

    return tags;
  }

  /**
   * Returns collection of contributors for this changeset.
   * @return collection of contributors
   * @since 2.1.0
   */
  public Collection<Contributor> getContributors() {
    if (contributors == null) {
      return new ArrayList<>();
    }
    return contributors;
  }

  /**
   * Returns true if the changeset is valid.
   *
   * @return true if the changeset is valid
   */
  @Override
  public boolean isValid() {
    return Util.isNotEmpty(id) && ValidationUtil.isValid(author)
      && (date != null);
  }

  /**
   * Sets the author of the changeset.
   */
  public void setAuthor(Person author) {
    this.author = author;
  }

  /**
   * Sets the branches of the changeset.
   */
  public void setBranches(List<String> branches) {
    this.branches = branches;
  }

  /**
   * Sets the date of the changeset.
   */
  public void setDate(Long date) {
    this.date = date;
  }

  /**
   * Sets the description (commit message) of the changeset.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the id of the changeset.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Sets the parents of the changeset.
   *
   * @param parents parents of the changeset
   * @since 1.11
   */
  public void setParents(List<String> parents) {
    this.parents = parents;
  }

  /**
   * Sets the tags of the changeset
   */
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * Sets the collection of contributors.
   *
   * @since 2.1.0
   */
  public void setContributors(Collection<Contributor> contributors) {
    this.contributors = new ArrayList<>(contributors);
  }

  /**
   * Adds a contributor to the list of contributors.
   *
   * @since 2.1.0
   */
  public void addContributor(Contributor contributor) {
    if (contributors == null) {
      contributors = new ArrayList<>();
    }
    contributors.add(contributor);
  }

  /**
   * Adds all contributors from the given collection to the list of contributors.
   *
   * @since 2.1.0
   */
  public void addContributors(Collection<Contributor> contributors) {
    if (this.contributors == null) {
      this.contributors = new ArrayList<>(contributors);
    } else {
      this.contributors.addAll(contributors);
    }
  }

  /**
   * Sets a collection of signatures which belong to this changeset.
   * @param signatures collection of signatures
   * @since 2.4.0
   */
  public void setSignatures(Collection<Signature> signatures) {
    this.signatures = new ArrayList<>(signatures);
  }

  /**
   * Returns a immutable list of signatures.
   * @return signatures
   * @since 2.4.0
   */
  public List<Signature> getSignatures() {
    return Collections.unmodifiableList(signatures);
  }

  /**
   * Adds a signature to the list of signatures.
   * @param signature
   * @since 2.4.0
   */
  public void addSignature(Signature signature) {
    signatures.add(signature);
  }
}
