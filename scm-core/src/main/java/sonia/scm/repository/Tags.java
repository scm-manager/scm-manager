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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

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
    return MoreObjects.toStringHelper(this)
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
