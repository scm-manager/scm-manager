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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.5
 */
@XmlRootElement(name = "file")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileObject implements LastModifiedAware
{

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

    final FileObject other = (FileObject) obj;

    return Objects.equal(name, other.name) && Objects.equal(path, other.path)
           && Objects.equal(directory, other.directory)
           && Objects.equal(description, other.description)
           && Objects.equal(length, other.length)
           && Objects.equal(subRepository, other.subRepository)
           && Objects.equal(lastModified, other.lastModified);
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
    return Objects.hashCode(name, path, directory, description, length,
                            subRepository, lastModified);
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
  @Override
  public Long getLastModified()
  {
    return lastModified;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public long getLength()
  {
    return length;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPath()
  {
    return path;
  }

  /**
   * Method description
   *
   * @since 1.10
   * @return
   */
  public SubRepository getSubRepository()
  {
    return subRepository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isDirectory()
  {
    return directory;
  }

  //~--- set methods ----------------------------------------------------------

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
   * @param directory
   */
  public void setDirectory(boolean directory)
  {
    this.directory = directory;
  }

  /**
   * Method description
   *
   *
   * @param lastModified
   */
  public void setLastModified(Long lastModified)
  {
    this.lastModified = lastModified;
  }

  /**
   * Method description
   *
   *
   * @param length
   */
  public void setLength(long length)
  {
    this.length = length;
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Method description
   *
   *
   * @param path
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  /**
   * Method description
   *
   *
   * @since 1.10
   *
   * @param subRepository
   */
  public void setSubRepository(SubRepository subRepository)
  {
    this.subRepository = subRepository;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String description;

  /** Field description */
  private boolean directory;

  /** Field description */
  private Long lastModified;

  /** Field description */
  private long length;

  /** Field description */
  private String name;

  /** Field description */
  private String path;

  /** Field description */
  @XmlElement(name = "subrepository")
  private SubRepository subRepository;
}
