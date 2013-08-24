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

import sonia.scm.LastModifiedAware;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
           && Objects.equal(lastModified, other.lastModified);
    //J+
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, path, directory, description, length,
                            subRepository, lastModified);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
            .add("name", name)
            .add("path", path)
            .add("directory", directory)
            .add("description", description)
            .add("length", length)
            .add("subRepository", subRepository)
            .add("lastModified", lastModified)
            .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the last commit message for this file. The method will return null,
   * if the repository provider is not able to get the last commit for the path.
   *
   *
   * @return last commit message
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the last commit date for this. The method will return null,
   * if the repository provider is not able to get the last commit for the path.
   *
   *
   * @return last commit date
   */
  @Override
  public Long getLastModified()
  {
    return lastModified;
  }

  /**
   * Returns the length of the file.
   *
   *
   * @return length of file
   */
  public long getLength()
  {
    return length;
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
   * Return sub repository informations or null if the file is not 
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
   * Sets the last modified date of the file.
   *
   *
   * @param lastModified last modified date
   */
  public void setLastModified(Long lastModified)
  {
    this.lastModified = lastModified;
  }

  /**
   * Sets the length of the file.
   *
   *
   * @param length file length
   */
  public void setLength(long length)
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

  //~--- fields ---------------------------------------------------------------

  /** file description */
  private String description;

  /** directory indicator */
  private boolean directory;

  /** last modified date */
  private Long lastModified;

  /** file length */
  private long length;

  /** filename */
  private String name;

  /** file path */
  private String path;

  /** sub repository informations */
  @XmlElement(name = "subrepository")
  private SubRepository subRepository;
}
