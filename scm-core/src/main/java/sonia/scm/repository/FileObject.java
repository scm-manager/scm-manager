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

import com.google.common.base.Strings;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sonia.scm.LastModifiedAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.ofNullable;

/**
 * The FileObject represents a file or a directory in a repository.
 *
 * @since 1.5
 */
@EqualsAndHashCode
@ToString
public class FileObject implements LastModifiedAware, Serializable {

  private static final long serialVersionUID = -5562537629609891499L;

  private String description;

  private boolean directory;

  private Long commitDate;

  private Long length;

  private String name;

  private String path;

  private boolean partialResult = false;

  private boolean computationAborted = false;

  @XmlElement(name = "subrepository")
  private SubRepository subRepository;

  private Collection<FileObject> children = new ArrayList<>();

  private boolean truncated;

  /**
   * Returns the last commit message for this file. The method will return null,
   * if the repository provider is not able to get the last commit for the path.
   *
   * @return Last commit message or <code>null</code>, when this value has not been computed
   * (see {@link #isPartialResult()}).
   */
  public Optional<String> getDescription() {
    return ofNullable(description);
  }

  /**
   * Returns the last commit date for this. The method will return null,
   * if the repository provider is not able to get the last commit for the path
   * or it has not been computed.
   *
   * @return last commit date
   */
  @Override
  public Long getLastModified() {
    return this.isPartialResult() ? null : this.commitDate;
  }

  /**
   * Returns the last commit date for this. The method will return {@link OptionalLong#empty()},
   * if the repository provider is not able to get the last commit for the path or if this value has not been computed
   * (see {@link #isPartialResult()} and {@link #isComputationAborted()}).
   */
  public OptionalLong getCommitDate() {
    return commitDate == null ? OptionalLong.empty() : OptionalLong.of(commitDate);
  }

  /**
   * Returns the length of the file or {@link OptionalLong#empty()}, when this value has not been computed
   * (see {@link #isPartialResult()} and {@link #isComputationAborted()}).
   */
  public OptionalLong getLength() {
    return length == null ? OptionalLong.empty() : OptionalLong.of(length);
  }

  /**
   * Returns the name of the file.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the path of the file.
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the parent path of the file.
   *
   * @return parent path
   */
  public String getParentPath() {
    if (Strings.isNullOrEmpty(path)) {
      return null;
    }
    int index = path.lastIndexOf('/');
    if (index > 0) {
      return path.substring(0, index);
    }
    return "";
  }

  /**
   * Return sub repository information or null if the file is not
   * sub repository.
   * @since 1.10
   */
  public SubRepository getSubRepository() {
    return subRepository;
  }

  /**
   * Returns true if the file is a directory.
   */
  public boolean isDirectory() {
    return directory;
  }

  /**
   * Returns the children of this file if it is a directory.
   */
  public Collection<FileObject> getChildren() {
    return children == null ? null : unmodifiableCollection(children);
  }

  /**
   * If this is <code>true</code>, some values for this object have not been computed, yet. These values (like
   * {@link #getLength()}, {@link #getDescription()} or {@link #getCommitDate()})
   * will return {@link Optional#empty()} (or {@link OptionalLong#empty()} respectively), unless they are computed.
   * There may be an asynchronous task running, that will set these values in the future.
   *
   * @return <code>true</code>, whenever some values of this object have not been computed, yet.
   * @since 2.0.0
   */
  public boolean isPartialResult() {
    return partialResult;
  }

  /**
   * If this is <code>true</code>, some values for this object have not been computed and will not be computed. These
   * values (like {@link #getLength()}, {@link #getDescription()} or {@link #getCommitDate()})
   * will return {@link Optional#empty()} (or {@link OptionalLong#empty()} respectively), unless they are computed.
   *
   * @return <code>true</code>, whenever some values of this object finally are not computed.
   * @since 2.0.0
   */
  public boolean isComputationAborted() {
    return computationAborted;
  }

  public boolean isTruncated() {
    return truncated;
  }


  /**
   * Sets the description of the file.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Set to true to indicate that the file is a directory.
   */
  public void setDirectory(boolean directory) {
    this.directory = directory;
  }

  /**
   * Sets the commit date of the file.
   */
  public void setCommitDate(Long commitDate) {
    this.commitDate = commitDate;
  }

  /**
   * Sets the length of the file.
   */
  public void setLength(Long length) {
    this.length = length;
  }

  /**
   * Sets the name of the file.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the path of the file.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Set sub repository information for the file.
   *
   *  @since 1.10
   */
  public void setSubRepository(SubRepository subRepository) {
    this.subRepository = subRepository;
  }

  /**
   * Set marker, that some values for this object are not computed, yet.
   *
   * @param partialResult Set this to <code>true</code>, whenever some values of this object are not computed, yet.
   * @since 2.0.0
   */
  public void setPartialResult(boolean partialResult) {
    this.partialResult = partialResult;
  }

  /**
   * Set marker, that computation of some values for this object has been aborted.
   *
   * @param computationAborted Set this to <code>true</code>, whenever some values of this object are not computed and
   *                           will not be computed in the future.
   * @since 2.0.0
   */
  public void setComputationAborted(boolean computationAborted) {
    this.computationAborted = computationAborted;
  }

  /**
   * Set the children for this file.
   */
  public void setChildren(List<FileObject> children) {
    this.children = new ArrayList<>(children);
  }

  /**
   * Adds a child to the list of children .
   *
   * @param child The additional child.
   */
  public void addChild(FileObject child) {
    this.children.add(child);
  }

  public void setTruncated(boolean truncated) {
    this.truncated = truncated;
  }
}
