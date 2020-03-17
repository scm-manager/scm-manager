/**
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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import sonia.scm.util.Util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "modifications")
public class Modifications implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = -8902033326668658140L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Modifications() {
  }

  /**
   * Constructs ...
   *
   *
   * @param added
   */
  public Modifications(List<String> added)
  {
    this(added, null, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param added
   * @param modified
   */
  public Modifications(List<String> added, List<String> modified)
  {
    this(added, modified, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param added
   * @param modified
   * @param removed
   */
  public Modifications(List<String> added, List<String> modified,
    List<String> removed)
  {
    this.added = added;
    this.modified = modified;
    this.removed = removed;
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

    final Modifications other = (Modifications) obj;

    return Objects.equal(added, other.added)
      && Objects.equal(modified, other.modified)
      && Objects.equal(removed, other.removed);
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
    return Objects.hashCode(added, modified, removed);
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
    StringBuilder out = new StringBuilder();

    out.append("added:").append(Util.toString(added)).append("\n");
    out.append("modified:").append(Util.toString(modified)).append("\n");
    out.append("removed:").append(Util.toString(removed)).append("\n");

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getAdded()
  {
    if (added == null)
    {
      added = Lists.newArrayList();
    }

    return added;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getModified()
  {
    if (modified == null)
    {
      modified = Lists.newArrayList();
    }

    return modified;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getRemoved()
  {
    if (removed == null)
    {
      removed = Lists.newArrayList();
    }

    return removed;
  }

  public String getRevision() {
    return revision;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param added
   */
  public void setAdded(List<String> added)
  {
    this.added = added;
  }

  /**
   * Method description
   *
   *
   * @param modified
   */
  public void setModified(List<String> modified)
  {
    this.modified = modified;
  }

  /**
   * Method description
   *
   *
   * @param removed
   */
  public void setRemoved(List<String> removed)
  {
    this.removed = removed;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  //~--- fields ---------------------------------------------------------------

  private String revision;

  /** list of added files */
  @XmlElement(name = "added")
  @XmlElementWrapper(name = "added")
  private List<String> added;

  /** list of modified files */
  @XmlElement(name = "modified")
  @XmlElementWrapper(name = "modified")
  private List<String> modified;

  /** list of removed files */
  @XmlElement(name = "removed")
  @XmlElementWrapper(name = "removed")
  private List<String> removed;
}
