/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents all tags of a repository.
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
@XmlRootElement(name = "tags")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Tags implements Iterable<Tag>
{

  /**
   * Constructs a new instance of tags.
   * This constructor should only be called from JAXB.
   *
   */
  public Tags() {}

  /**
   * Constructs a new instance of tags.
   *
   *
   * @param tags list of tags.
   */
  public Tags(List<Tag> tags)
  {
    this.tags = tags;
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

    final Tags other = (Tags) obj;

    return Objects.equal(tags, other.tags);
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
    return Objects.hashCode(tags);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public Iterator<Tag> iterator()
  {
    return getTags().iterator();
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
                  .add("tags", tags)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the {@link Tag} with the given name or null.
   *
   *
   * @param name name of the tag
   *
   * @return {@link Tag} with the given name or null
   */
  public Tag getTagByName(String name)
  {
    Tag tag = null;

    for (Tag t : getTags())
    {
      if (name.equals(t.getName()))
      {
        tag = t;

        break;
      }
    }

    return tag;
  }

  /**
   * Returns the {@link Tag} with the given revision or null.
   *
   *
   * @param revision revision of the tag
   *
   * @return {@link Tag} with the given revision or null
   */
  public Tag getTagByRevision(String revision)
  {
    Tag tag = null;

    for (Tag t : getTags())
    {
      if (revision.equals(t.getRevision()))
      {
        tag = t;

        break;
      }
    }

    return tag;
  }

  /**
   * Returns all tags of a repository.
   *
   *
   * @return all tags
   */
  public List<Tag> getTags()
  {
    if (tags == null)
    {
      tags = Lists.newArrayList();
    }

    return tags;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets all tags.
   *
   *
   * @param tags tags
   */
  public void setTags(List<Tag> tags)
  {
    this.tags = tags;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "tag")
  private List<Tag> tags;
}
