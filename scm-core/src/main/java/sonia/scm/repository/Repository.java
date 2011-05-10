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

import sonia.scm.ModelObject;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "repositories")
@XmlAccessorType(XmlAccessType.FIELD)
public class Repository implements ModelObject
{

  /** Field description */
  private static final long serialVersionUID = 3486560714961909711L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Repository() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param id
   * @param type
   * @param name
   */
  public Repository(String id, String type, String name)
  {
    this.id = id;
    this.type = type;
    this.name = name;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param id
   * @param type
   * @param name
   * @param contact
   * @param description
   * @param permissions
   */
  public Repository(String id, String type, String name, String contact,
                    String description, Permission... permissions)
  {
    this.id = id;
    this.type = type;
    this.name = name;
    this.contact = contact;
    this.description = description;
    this.permissions = new ArrayList<Permission>();

    if (Util.isNotEmpty(permissions))
    {
      this.permissions.addAll(Arrays.asList(permissions));
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Repository clone()
  {
    Repository repository = null;

    try
    {
      repository = (Repository) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      throw new RuntimeException(ex);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  public void copyProperties(Repository repository)
  {
    repository.setName(name);
    repository.setContact(contact);
    repository.setCreationDate(creationDate);
    repository.setLastModified(lastModified);
    repository.setDescription(description);
    repository.setPermissions(permissions);
    repository.setUrl(url);
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

    final Repository other = (Repository) obj;

    if ((this.contact == null)
        ? (other.contact != null)
        : !this.contact.equals(other.contact))
    {
      return false;
    }

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

    if ((this.id == null)
        ? (other.id != null)
        : !this.id.equals(other.id))
    {
      return false;
    }

    if ((this.lastModified != other.lastModified)
        && ((this.lastModified == null)
            ||!this.lastModified.equals(other.lastModified)))
    {
      return false;
    }

    if ((this.name == null)
        ? (other.name != null)
        : !this.name.equals(other.name))
    {
      return false;
    }

    if ((this.permissions != other.permissions)
        && ((this.permissions == null)
            ||!this.permissions.equals(other.permissions)))
    {
      return false;
    }

    if (this.publicReadable != other.publicReadable)
    {
      return false;
    }

    if ((this.type == null)
        ? (other.type != null)
        : !this.type.equals(other.type))
    {
      return false;
    }

    if ((this.url == null)
        ? (other.url != null)
        : !this.url.equals(other.url))
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

    hash = 61 * hash + ((this.contact != null)
                        ? this.contact.hashCode()
                        : 0);
    hash = 61 * hash + ((this.creationDate != null)
                        ? this.creationDate.hashCode()
                        : 0);
    hash = 61 * hash + ((this.description != null)
                        ? this.description.hashCode()
                        : 0);
    hash = 61 * hash + ((this.id != null)
                        ? this.id.hashCode()
                        : 0);
    hash = 61 * hash + ((this.lastModified != null)
                        ? this.lastModified.hashCode()
                        : 0);
    hash = 61 * hash + ((this.name != null)
                        ? this.name.hashCode()
                        : 0);
    hash = 61 * hash + ((this.permissions != null)
                        ? this.permissions.hashCode()
                        : 0);
    hash = 61 * hash + (this.publicReadable
                        ? 1
                        : 0);
    hash = 61 * hash + ((this.type != null)
                        ? this.type.hashCode()
                        : 0);
    hash = 61 * hash + ((this.url != null)
                        ? this.url.hashCode()
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
    StringBuilder buffer = new StringBuilder("Repository{type=");

    buffer.append(type).append(", name=").append(name).append("}");

    return buffer.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContact()
  {
    return contact;
  }

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
  public String getId()
  {
    return id;
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
  public List<Permission> getPermissions()
  {
    return permissions;
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
   * @return
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPublicReadable()
  {
    return publicReadable;
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
    return Util.isNotEmpty(name) && Util.isNotEmpty(type)
           && ((Util.isEmpty(contact))
               || ValidationUtil.isMailAddressValid(contact));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param contact
   */
  public void setContact(String contact)
  {
    this.contact = contact;
  }

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
   * @param id
   */
  public void setId(String id)
  {
    this.id = id;
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
   * @param permissions
   */
  public void setPermissions(List<Permission> permissions)
  {
    this.permissions = permissions;
  }

  /**
   * Method description
   *
   *
   * @param publicReadable
   */
  public void setPublicReadable(boolean publicReadable)
  {
    this.publicReadable = publicReadable;
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

  /**
   * Method description
   *
   *
   * @param url
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String contact;

  /** Field description */
  private Long creationDate;

  /** Field description */
  private String description;

  /** Field description */
  private String id;

  /** Field description */
  private Long lastModified;

  /** Field description */
  private String name;

  /** Field description */
  private List<Permission> permissions;

  /** Field description */
  @XmlElement(name = "public")
  private boolean publicReadable = false;

  /** Field description */
  private String type;

  /** Field description */
  private String url;
}
