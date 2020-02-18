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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import sonia.scm.LastModifiedAware;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@XmlRootElement(name = "file")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileObject implements LastModifiedAware, Serializable
{

  /** serial version uid */
  private static final long serialVersionUID = -5562537629609891499L;

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
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

    final FileObject other = (FileObject) obj;

    //J-
    return Objects.equal(name, other.name)
           && Objects.equal(path, other.path)
           && Objects.equal(directory, other.directory)
           && Objects.equal(description, other.description)
           && Objects.equal(length, other.length)
           && Objects.equal(subRepository, other.subRepository)
           && Objects.equal(commitDate, other.commitDate)
           && Objects.equal(partialResult, other.partialResult)
           && Objects.equal(computationAborted, other.computationAborted);
    //J+
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(
      name,
      path,
      directory,
      description,
      length,
      subRepository,
      commitDate,
      partialResult,
      computationAborted);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("path", path)
            .add("directory", directory)
            .add("description", description)
            .add("length", length)
            .add("subRepository", subRepository)
            .add("commitDate", commitDate)
            .add("partialResult", partialResult)
            .add("computationAborted", computationAborted)
            .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the last commit message for this file. The method will return null,
   * if the repository provider is not able to get the last commit for the path.
   *
   *
   * @return Last commit message or <code>null</code>, when this value has not been computed
   * (see {@link #isPartialResult()}).
   */
  public Optional<String> getDescription()
  {
    return ofNullable(description);
  }

  /**
   * Returns the last commit date for this. The method will return null,
   * if the repository provider is not able to get the last commit for the path
   * or it has not been computed.
   *
   *
   * @return last commit date
   */
  @Override
  public Long getLastModified() {
    return this.isPartialResult()? null: this.commitDate;
  }

  /**
   * Returns the last commit date for this. The method will return {@link OptionalLong#empty()},
   * if the repository provider is not able to get the last commit for the path or if this value has not been computed
   * (see {@link #isPartialResult()} and {@link #isComputationAborted()}).
   */
  public OptionalLong getCommitDate()
  {
    return commitDate == null? OptionalLong.empty(): OptionalLong.of(commitDate);
  }

  /**
   * Returns the length of the file or {@link OptionalLong#empty()}, when this value has not been computed
   * (see {@link #isPartialResult()} and {@link #isComputationAborted()}).
   */
  public OptionalLong getLength()
  {
    return length == null? OptionalLong.empty(): OptionalLong.of(length);
  }

  /**
   * Returns the name of the file.
   *
   *
   * @return name of file
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the path of the file.
   *
   *
   * @return path of file
   */
  public String getPath()
  {
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
   * @since 1.10
   * @return sub repository informations or null
   */
  public SubRepository getSubRepository()
  {
    return subRepository;
  }

  /**
   * Returns true if the file is a directory.
   *
   *
   * @return true if file is a directory
   */
  public boolean isDirectory()
  {
    return directory;
  }

  /**
   * Returns the children of this file.
   *
   * @return The children of this file if it is a directory.
   */
  public Collection<FileObject> getChildren() {
    return children == null? null: unmodifiableCollection(children);
  }

  /**
   * If this is <code>true</code>, some values for this object have not been computed, yet. These values (like
   * {@link #getLength()}, {@link #getDescription()} or {@link #getCommitDate()})
   * will return {@link Optional#empty()} (or {@link OptionalLong#empty()} respectively), unless they are computed.
   * There may be an asynchronous task running, that will set these values in the future.
   *
   * @since 2.0.0
   *
   * @return <code>true</code>, whenever some values of this object have not been computed, yet.
   */
  public boolean isPartialResult() {
    return partialResult;
  }

  /**
   * If this is <code>true</code>, some values for this object have not been computed and will not be computed. These
   * values (like {@link #getLength()}, {@link #getDescription()} or {@link #getCommitDate()})
   * will return {@link Optional#empty()} (or {@link OptionalLong#empty()} respectively), unless they are computed.
   *
   * @since 2.0.0
   *
   * @return <code>true</code>, whenever some values of this object finally are not computed.
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
   *
   * @param description description of file
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Set to true to indicate that the file is a directory.
   *
   *
   * @param directory true for directory
   */
  public void setDirectory(boolean directory)
  {
    this.directory = directory;
  }

  /**
   * Sets the commit date of the file.
   *
   *
   * @param commitDate commit date
   */
  public void setCommitDate(Long commitDate)
  {
    this.commitDate = commitDate;
  }

  /**
   * Sets the length of the file.
   *
   *
   * @param length file length
   */
  public void setLength(Long length)
  {
    this.length = length;
  }

  /**
   * Sets the name of the file.
   *
   *
   * @param name filename
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Sets the path of the file.
   *
   *
   * @param path file path
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  /**
   * Set sub repository information for the file.
   *
   * @since 1.10
   *
   * @param subRepository sub repository informations
   */
  public void setSubRepository(SubRepository subRepository)
  {
    this.subRepository = subRepository;
  }

  /**
   * Set marker, that some values for this object are not computed, yet.
   *
   * @since 2.0.0
   *
   * @param partialResult Set this to <code>true</code>, whenever some values of this object are not computed, yet.
   */
  public void setPartialResult(boolean partialResult) {
    this.partialResult = partialResult;
  }

  /**
   * Set marker, that computation of some values for this object has been aborted.
   *
   * @since 2.0.0
   *
   * @param computationAborted Set this to <code>true</code>, whenever some values of this object are not computed and
   *                           will not be computed in the future.
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

  //~--- fields ---------------------------------------------------------------

  /** file description */
  private String description;

  /** directory indicator */
  private boolean directory;

  /** commit date */
  private Long commitDate;

  /** file length */
  private Long length;

  /** filename */
  private String name;

  /** file path */
  private String path;

  /** Marker for partial result. */
  private boolean partialResult = false;

  /** Marker for aborted computation. */
  private boolean computationAborted = false;

  /** sub repository informations */
  @XmlElement(name = "subrepository")
  private SubRepository subRepository;

  /** Children of this file (aka directory). */
  private Collection<FileObject> children = new ArrayList<>();

  private boolean truncated;
}
