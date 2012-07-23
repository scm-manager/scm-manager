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

import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.util.HttpUtil;
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
 * Source code repository.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "repositories")
@XmlAccessorType(XmlAccessType.FIELD)
public class Repository extends BasicPropertiesAware implements ModelObject
{

  /** Field description */
  private static final long serialVersionUID = 3486560714961909711L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link Repository}.
   * This constructor is used by JAXB.
   *
   */
  public Repository() {}

  /**
   * Constructs a new {@link Repository}.
   *
   *
   *
   * @param id id of the {@link Repository}
   * @param type type of the {@link Repository}
   * @param name name of the {@link Repository}
   */
  public Repository(String id, String type, String name)
  {
    this.id = id;
    this.type = type;
    this.name = name;
  }

  /**
   * Constructs a new {@link Repository}.
   *
   *
   *
   * @param id id of the {@link Repository}
   * @param type type of the {@link Repository}
   * @param name name of the {@link Repository}
   * @param contact email address of a person who is responsible for
   *        this repository.
   * @param description a short description of the repository
   * @param permissions permissions for specific users and groups.
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
   * Create a clone of this {@link Repository} object.
   *
   *
   * @return clone of this {@link Repository}
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
   * Copies all properties of the {@link Repository} to the given one.
   *
   *
   * @param repository to copies all properties of this one
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
    repository.setPublicReadable(publicReadable);
    repository.setArchived(archived);
  }

  /**
   * Creates the url of the repository.
   *
   *
   * @param baseUrl base url of the server including the context path
   *
   * @return url of the repository
   * @since 1.17
   */
  public String createUrl(String baseUrl)
  {
    String url = HttpUtil.append(baseUrl, type);

    return HttpUtil.append(url, name);
  }

  /**
   * Returns true if the {@link Repository} is the same as the obj argument.
   *
   *
   * @param obj the reference object with which to compare
   *
   * @return true if the {@link Repository} is the same as the obj argument
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

    //J-
    return Objects.equal(id, other.id) 
           && Objects.equal(name, other.name)
           && Objects.equal(contact, other.contact)
           && Objects.equal(description, other.description)
           && Objects.equal(publicReadable, other.publicReadable)
           && Objects.equal(archived, other.archived)
           && Objects.equal(permissions, other.permissions)
           && Objects.equal(type, other.type) 
           && Objects.equal(url, other.url)
           && Objects.equal(creationDate, other.creationDate)
           && Objects.equal(lastModified, other.lastModified)
           && Objects.equal(properties, other.properties);
    //J+
  }

  /**
   * Returns the hash code value for the {@link Repository}.
   *
   *
   * @return the hash code value for the {@link Repository}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(id, name, contact, description, publicReadable,
                            archived, permissions, type, url, creationDate,
                            lastModified, properties);
  }

  /**
   * Returns a {@link String} that represents the {@link Repository}.
   *
   *
   * @return {@link String} that represents the {@link Repository}
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
            .add("id", id)
            .add("name", name)
            .add("contact", contact)
            .add("description", description)
            .add("publicReadable", publicReadable)
            .add("archived", archived)
            .add("permissions", permissions)
            .add("type", type)
            .add("url", url)
            .add("lastModified", lastModified)
            .add("creationDate", creationDate)
            .add("properties", properties)
            .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a contact email address of a person who is responsible for
   * the {@link Repository}.
   *
   *
   * @return contact email address
   */
  public String getContact()
  {
    return contact;
  }

  /**
   * Returns a timestamp of the creation date of the {@link Repository}.
   *
   *
   * @return a timestamp of the creation date of the {@link Repository}
   */
  public Long getCreationDate()
  {
    return creationDate;
  }

  /**
   * Returns a short description of the {@link Repository}.
   *
   *
   * @return short description
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the unique id of the {@link Repository}.
   *
   *
   * @return unique id
   */
  @Override
  public String getId()
  {
    return id;
  }

  /**
   * Returns the timestamp of the last modified date of the {@link Repository}.
   *
   *
   * @return timestamp of the last modified date
   */
  @Override
  public Long getLastModified()
  {
    return lastModified;
  }

  /**
   * Returns the name of the {@link Repository}.
   *
   *
   * @return name of the {@link Repository}
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the access permissions of the {@link Repository}.
   *
   *
   * @return access permissions
   */
  public List<Permission> getPermissions()
  {
    return permissions;
  }

  /**
   * Returns the type (hg, git, svn ...) of the {@link Repository}.
   *
   *
   * @return type of the repository
   */
  @Override
  public String getType()
  {
    return type;
  }

  /**
   * Returns the base url of the {@link Repository}.
   *
   *
   * @return base url
   * @deprecated use {@link #createUrl(String)}
   */
  @Deprecated
  public String getUrl()
  {
    return url;
  }

  /**
   * Returns true if the repository is archived.
   *
   *
   * @return true if the repository is archived
   * @since 1.14
   */
  public boolean isArchived()
  {
    return archived;
  }

  /**
   * Returns true if the {@link Repository} is public readable.
   *
   *
   * @return true if the {@link Repository} is public readable
   */
  public boolean isPublicReadable()
  {
    return publicReadable;
  }

  /**
   * Returns true if the {@link Repository} is valid.
   * <ul>
   *   <li>The name is not empty and contains only A-z, 0-9, _, -, /</li>
   *   <li>The type is not empty</li>
   *   <li>The contact is empty or contains a valid email address</li>
   * </ul>
   *
   *
   * @return true if the {@link Repository} is valid
   */
  @Override
  public boolean isValid()
  {
    return ValidationUtil.isRepositoryNameValid(name) && Util.isNotEmpty(type)
           && ((Util.isEmpty(contact))
               || ValidationUtil.isMailAddressValid(contact));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Archive or un archive this repository.
   *
   *
   * @param archived true to enable archive
   * @since 1.14
   */
  public void setArchived(boolean archived)
  {
    this.archived = archived;
  }

  /**
   * Sets the contact of the {@link Repository}. The contact address should be
   * a email address of a person who is responsible for the {@link Repository}.
   *
   *
   * @param contact email address of a person who is responsible for
   *  the {@link Repository}
   */
  public void setContact(String contact)
  {
    this.contact = contact;
  }

  /**
   * Set the creation date of the {@link Repository}.
   *
   *
   * @param creationDate creation date of the {@link Repository}
   */
  public void setCreationDate(Long creationDate)
  {
    this.creationDate = creationDate;
  }

  /**
   * Sets a short description of the {@link Repository}.
   *
   *
   * @param description short description
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * The unique id of the {@link Repository}.
   *
   *
   * @param id unique id
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Set the last modified timestamp of the {@link Repository}.
   *
   *
   * @param lastModified last modified timestamp
   */
  public void setLastModified(Long lastModified)
  {
    this.lastModified = lastModified;
  }

  /**
   * Set the name of the {@link Repository}.
   *
   *
   * @param name name of the {@link Repository}
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Set the access permissions for the {@link Repository}.
   *
   *
   * @param permissions list of access permissions
   */
  public void setPermissions(List<Permission> permissions)
  {
    this.permissions = permissions;
  }

  /**
   * Sets true if the {@link Repository} is public readable.
   *
   *
   * @param publicReadable public readable
   */
  public void setPublicReadable(boolean publicReadable)
  {
    this.publicReadable = publicReadable;
  }

  /**
   * Sets the type (hg, svn, git ...) of the {@link Repository}.
   *
   *
   * @param type type of the {@link Repository}
   */
  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * Sets the base url of the {@link Repository}
   *
   *
   * @param url base url
   * @deprecated
   */
  @Deprecated
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
  private boolean archived = false;

  /** Field description */
  private String type;

  /**
   * @deprecated use {@link #createUrl(java.lang.String)} instead
   */
  private String url;
}
