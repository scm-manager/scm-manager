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

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

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
  public Modifications() {}

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
   * Method description
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

    if ((this.added != other.added)
        && ((this.added == null) ||!this.added.equals(other.added)))
    {
      return false;
    }

    if ((this.modified != other.modified)
        && ((this.modified == null) ||!this.modified.equals(other.modified)))
    {
      return false;
    }

    if ((this.removed != other.removed)
        && ((this.removed == null) ||!this.removed.equals(other.removed)))
    {
      return false;
    }

    return true;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    int hash = 7;

    hash = 41 * hash + ((this.added != null)
                        ? this.added.hashCode()
                        : 0);
    hash = 41 * hash + ((this.modified != null)
                        ? this.modified.hashCode()
                        : 0);
    hash = 41 * hash + ((this.removed != null)
                        ? this.removed.hashCode()
                        : 0);

    return hash;
  }

  /**
   * Method description
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
      added = new ArrayList<String>();
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
      modified = new ArrayList<String>();
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
      removed = new ArrayList<String>();
    }

    return removed;
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

  //~--- fields ---------------------------------------------------------------

  /** list of added files */
  @XmlElement(name = "file")
  @XmlElementWrapper(name = "added")
  public List<String> added;

  /** list of modified files */
  @XmlElement(name = "file")
  @XmlElementWrapper(name = "modified")
  public List<String> modified;

  /** list of removed files */
  @XmlElement(name = "file")
  @XmlElementWrapper(name = "removed")
  public List<String> removed;
}
