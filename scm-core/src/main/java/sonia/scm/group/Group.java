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

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Organizes users into a group for easier permissions management.
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
   * Constructs {@link Group} object. This constructor is required by JAXB.
   *
   */
  public Group() {}

  /**
   * Constructs {@link Group} object.
   *
   *
   *
   * @param type of the group
   * @param name of the group
   */
  public Group(String type, String name)
  {
    this.type = type;
    this.name = name;
    this.members = new ArrayList<String>();
  }

  /**
   * Constructs {@link Group} object.
   *
   *
   *
   * @param type of the group
   * @param name of the group
   * @param members of the groups
   */
  public Group(String type, String name, List<String> members)
  {
    this.type = type;
    this.name = name;
    this.members = members;
  }

  /**
   * Constructs {@link Group} object.
   *
   *
   *
   * @param type of the group
   * @param name of the group
   * @param members of the groups
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
   * Add a new member to the group.
   *
   *
   * @param member - The name of new group member
   *
   * @return true if the operation was successful
   */
  public boolean add(String member)
  {
    return members.add(member);
  }

  /**
   * Remove all members of the group.
   *
   */
  public void clear()
  {
    members.clear();
  }

  /**
   * Returns a clone of the group.
   *
   *
   * @return a clone of the group
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
   * Copies all properties of this group to the given one.
   *
   *
   * @param group to copies all properties of this one
   */
  public void copyProperties(Group group)
  {
    group.setName(name);
    group.setMembers(members);
    group.setType(type);
    group.setDescription(description);
  }

  /**
   * Returns true if this {@link Group} is the same as the obj argument.
   *
   *
   * @param obj - the reference object with which to compare
   *
   * @return true if this {@link Group} is the same as the obj argument
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
   * Returns a hash code value for this {@link Group}.
   *
   *
   * @return a hash code value for this {@link Group}
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
   * Returns a {@link java.util.Iterator} for the members of this {@link Group}.
   *
   *
   * @return a {@link java.util.Iterator} for the members of this {@link Group}
   */
  @Override
  public Iterator<String> iterator()
  {
    return getMembers().iterator();
  }

  /**
   * Remove the given member from this group.
   *
   *
   * @param member to remove from this group
   *
   * @return true if the operation was successful
   */
  public boolean remove(String member)
  {
    return members.remove(member);
  }

  /**
   * Returns a {@link String} that represents this group.
   *
   *
   * @return a {@link String} that represents this group
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
   * Returns a timestamp of the creation date of this group.
   *
   *
   * @return a timestamp of the creation date of this group
   */
  public Long getCreationDate()
  {
    return creationDate;
  }

  /**
   * Returns the description of this group.
   *
   *
   * @return  the description of this group
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the unique name of this group. This method is an alias for the
   * {@link #getName()} method.
   *
   *
   * @return the unique name of this group
   */
  @Override
  public String getId()
  {
    return name;
  }

  /**
   * Returns a timestamp of the last modified date of this group.
   *
   *
   * @return a timestamp of the last modified date of this group
   */
  @Override
  public Long getLastModified()
  {
    return lastModified;
  }

  /**
   * Returns a {@link java.util.List} of all members of this group.
   *
   *
   * @return a {@link java.util.List} of all members of this group
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
   * Returns the unique name of this group.
   *
   *
   * @return the unique name of this group
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the type of this group. The default type is xml.
   *
   *
   * @return the type of this group
   */
  @Override
  public String getType()
  {
    return type;
  }

  /**
   * Returns true if the member is a member of this group.
   *
   *
   * @param member - The name of the member
   *
   * @return true if the member is a member of this group
   */
  public boolean isMember(String member)
  {
    return (members != null) && members.contains(member);
  }

  /**
   * Returns true if the group is valid.
   *
   *
   * @return true if the group is valid
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(name) && Util.isNotEmpty(type);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the date the group was created.
   *
   *
   * @param creationDate - date the group was last modified
   */
  public void setCreationDate(Long creationDate)
  {
    this.creationDate = creationDate;
  }

  /**
   * Sets the description of the group.
   *
   *
   * @param description of the group
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Sets the date the group was last modified.
   *
   *
   * @param lastModified - date the group was last modified
   */
  public void setLastModified(Long lastModified)
  {
    this.lastModified = lastModified;
  }

  /**
   * Sets the members of the group.
   *
   *
   * @param members of the group
   */
  public void setMembers(List<String> members)
  {
    this.members = members;
  }

  /**
   * Sets the name of the group.
   *
   *
   * @param name of the group
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Sets the type of the group.
   *
   *
   * @param type of the group
   */
  public void setType(String type)
  {
    this.type = type;
  }

  //~--- fields ---------------------------------------------------------------

  /** timestamp of the creation date of this group */
  private Long creationDate;

  /** description of this group */
  private String description;

  /** timestamp of the last modified date of this group */
  private Long lastModified;

  /** members of this group */
  private List<String> members;

  /** name of this group */
  private String name;

  /** type of this group */
  private String type;
}
