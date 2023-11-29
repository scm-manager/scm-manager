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
 * @author Sebastian Sdorra
 * @since 1.5
 */
@EqualsAndHashCode
@ToString
public class FileObject implements LastModifiedAware, Serializable {

  private static final long serialVersionUID = -5562537629609891499L;

  private String description;

  /**
   * directory indicator
   */
  private boolean directory;

  /**
   * commit date
   */
  private Long commitDate;

  /**
   * file length
   */
  private Long length;

  /**
   * filename
   */
  private String name;

  /**
   * file path
   */
  private String path;

  /**
   * Marker for partial result.
   */
  private boolean partialResult = false;

  /**
   * Marker for aborted computation.
   */
  private boolean computationAborted = false;

  /**
   * sub repository informations
   */
  @XmlElement(name = "subrepository")
  private SubRepository subRepository;

  /**
   * Children of this file (aka directory).
   */
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
   *
   * @return name of file
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the path of the file.
   *
   * @return path of file
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
   *
   * @return sub repository informations or null
   * @since 1.10
   */
  public SubRepository getSubRepository() {
    return subRepository;
  }

  /**
   * Returns true if the file is a directory.
   *
   * @return true if file is a directory
   */
  public boolean isDirectory() {
    return directory;
  }

  /**
   * Returns the children of this file.
   *
   * @return The children of this file if it is a directory.
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the description of the file.
   *
   * @param description description of file
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Set to true to indicate that the file is a directory.
   *
   * @param directory true for directory
   */
  public void setDirectory(boolean directory) {
    this.directory = directory;
  }

  /**
   * Sets the commit date of the file.
   *
   * @param commitDate commit date
   */
  public void setCommitDate(Long commitDate) {
    this.commitDate = commitDate;
  }

  /**
   * Sets the length of the file.
   *
   * @param length file length
   */
  public void setLength(Long length) {
    this.length = length;
  }

  /**
   * Sets the name of the file.
   *
   * @param name filename
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the path of the file.
   *
   * @param path file path
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Set sub repository information for the file.
   *
   * @param subRepository sub repository informations
   * @since 1.10
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
   *
   * @param children The new childre.
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
