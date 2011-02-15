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



package sonia.scm.group;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ModelObject;
import sonia.scm.util.Util;
import sonia.scm.xml.XmlTimestampDateAdapter;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "groups")
@XmlAccessorType(XmlAccessType.FIELD)
public class Group implements ModelObject, Iterable<String>
{

  /** Field description */
  private static final long serialVersionUID = 1752369869345245872L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Group() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param type
   * @param name
   */
  public Group(String type, String name)
  {
    this.type = type;
    this.name = name;
    this.members = new ArrayList<String>();
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param type
   * @param name
   * @param members
   */
  public Group(String type, String name, List<String> members)
  {
    this.type = type;
    this.name = name;
    this.members = members;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param type
   * @param name
   * @param members
   */
  public Group(String type, String name, String... members)
  {
    this.type = type;
    this.name = name;
    this.members = new ArrayList<String>();

    if (Util.isNotEmpty(members))
    {
      this.members.addAll(Arrays.asList(members));
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  public boolean add(String member)
  {
    return members.add(member);
  }

  /**
   * Method description
   *
   */
  public void clear()
  {
    members.clear();
  }

  /**
   * Method description
   *
   *
   * @return
   *
   */
  @Override
  public Group clone()
  {
    Group group = null;

    try
    {
      group = (Group) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      throw new RuntimeException(ex);
    }

    return group;
  }

  /**
   * Method description
   *
   *
   * @param group
   */
  public void copyProperties(Group group)
  {
    group.setName(name);
    group.setMembers(members);
    group.setType(type);
    group.setDescription(description);
  }

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

    final Group other = (Group) obj;

    if ((this.creationDate != other.creationDate)
        && ((this.creationDate == null)
            ||!this.creationDate.equals(other.creationDate)))
    {
      return false;
    }

    if ((this.description == null)
        ? (other.description != null)
        : !this.description.equals(other.description))
    {
      return false;
    }

    if ((this.lastModified != other.lastModified)
        && ((this.lastModified == null)
            ||!this.lastModified.equals(other.lastModified)))
    {
      return false;
    }

    if ((this.members != other.members)
        && ((this.members == null) ||!this.members.equals(other.members)))
    {
      return false;
    }

    if ((this.name == null)
        ? (other.name != null)
        : !this.name.equals(other.name))
    {
      return false;
    }

    if ((this.type == null)
        ? (other.type != null)
        : !this.type.equals(other.type))
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

    hash = 73 * hash + ((this.creationDate != null)
                        ? this.creationDate.hashCode()
                        : 0);
    hash = 73 * hash + ((this.description != null)
                        ? this.description.hashCode()
                        : 0);
    hash = 73 * hash + ((this.lastModified != null)
                        ? this.lastModified.hashCode()
                        : 0);
    hash = 73 * hash + ((this.members != null)
                        ? this.members.hashCode()
                        : 0);
    hash = 73 * hash + ((this.name != null)
                        ? this.name.hashCode()
                        : 0);
    hash = 73 * hash + ((this.type != null)
                        ? this.type.hashCode()
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
  public Iterator<String> iterator()
  {
    return getMembers().iterator();
  }

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  public boolean remove(String member)
  {
    return members.remove(member);
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
    StringBuilder msg = new StringBuilder();

    msg.append(name).append(" [");

    if (Util.isNotEmpty(members))
    {
      Iterator<String> it = members.iterator();

      while (it.hasNext())
      {
        msg.append(it.next());

        if (it.hasNext())
        {
          msg.append(",");
        }
      }
    }

    return msg.append("]").toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getCreationDate()
  {
    return creationDate;
  }

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
  public List<String> getMembers()
  {
    if (members == null)
    {
      members = new ArrayList<String>();
    }

    return members;
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
  @Override
  public String getType()
  {
    return type;
  }

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  public boolean isMember(String member)
  {
    return (members != null) && members.contains(member);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(name) && Util.isNotEmpty(type);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param creationDate
   */
  public void setCreationDate(Long creationDate)
  {
    this.creationDate = creationDate;
  }

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
   * @param members
   */
  public void setMembers(List<String> members)
  {
    this.members = members;
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
   * @param type
   */
  public void setType(String type)
  {
    this.type = type;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlJavaTypeAdapter(XmlTimestampDateAdapter.class)
  private Long creationDate;

  /** Field description */
  private String description;

  /** Field description */
  @XmlJavaTypeAdapter(XmlTimestampDateAdapter.class)
  private Long lastModified;

  /** Field description */
  private List<String> members;

  /** Field description */
  private String name;

  /** Field description */
  private String type;
}
