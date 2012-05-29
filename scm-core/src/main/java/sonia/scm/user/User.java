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



package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

//~--- JDK imports ------------------------------------------------------------

import java.security.Principal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
public class User extends BasicPropertiesAware implements Principal, ModelObject
{

  /** Field description */
  private static final long serialVersionUID = -3089541936726329663L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public User() {}

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  public User(String name)
  {
    this.name = name;
    this.displayName = name;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param displayName
   * @param mail
   */
  public User(String name, String displayName, String mail)
  {
    this.name = name;
    this.displayName = displayName;
    this.mail = mail;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   */
  @Override
  public User clone()
  {
    User user = null;

    try
    {
      user = (User) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      throw new RuntimeException(ex);
    }

    return user;
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  public boolean copyProperties(User user)
  {
    return copyProperties(user, true);
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param copyPassword
   *
   * @return
   */
  public boolean copyProperties(User user, boolean copyPassword)
  {
    boolean result = false;

    if (user.isAdmin() != admin)
    {
      result = true;
      user.setAdmin(admin);
    }

    if (user.isActive() != active)
    {
      result = true;
      user.setActive(active);
    }

    if (Util.isNotEquals(user.getDisplayName(), displayName))
    {
      result = true;
      user.setDisplayName(displayName);
    }

    if (Util.isNotEquals(user.getMail(), mail))
    {
      result = true;
      user.setMail(mail);
    }

    if (Util.isNotEquals(user.getName(), name))
    {
      result = true;
      user.setName(name);
    }

    if (copyPassword && Util.isNotEquals(user.getPassword(), password))
    {
      result = true;
      user.setPassword(password);
    }

    if (Util.isNotEquals(user.getType(), type))
    {
      result = true;
      user.setType(type);
    }

    return result;
  }

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

    final User other = (User) obj;

    return Objects.equal(name, other.name)
           && Objects.equal(displayName, other.displayName)
           && Objects.equal(mail, other.mail)
           && Objects.equal(type, other.type)
           && Objects.equal(admin, other.admin)
           && Objects.equal(active, other.active)
           && Objects.equal(password, other.password)
           && Objects.equal(creationDate, other.creationDate)
           && Objects.equal(lastModified, other.lastModified)
           && Objects.equal(properties, other.properties);
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
    return Objects.hashCode(name, displayName, mail, type, admin, password,
                            active, creationDate, lastModified, properties);
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
    String pwd = (password != null)
                 ? "(is set)"
                 : "(not set)";

    //J-
    return Objects.toStringHelper(this)
            .add("name", name)
            .add("displayName",displayName)
            .add("mail", mail)
            .add("password", pwd)
            .add("admin", admin)
            .add("type", type)
            .add("active", active)
            .add("creationDate", creationDate)
            .add("lastModified", lastModified)
            .add("properties", properties)
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
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getId()
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
  public String getMail()
  {
    return mail;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
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
  public String getPassword()
  {
    return password;
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
   * Returns false if the user is deactivated.
   *
   *
   * @return false if the user is deactivated
   * @since 1.16
   */
  public boolean isActive()
  {
    return active;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isAdmin()
  {
    return admin;
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
    return ValidationUtil.isUsernameValid(name) && Util.isNotEmpty(displayName)
           && Util.isNotEmpty(type)
           && ((Util.isEmpty(mail)) || ValidationUtil.isMailAddressValid(mail));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Activate or deactive this user.
   *
   *
   * @param active false to deactivate the user.
   * @since 1.6
   */
  public void setActive(boolean active)
  {
    this.active = active;
  }

  /**
   * Method description
   *
   *
   * @param admin
   */
  public void setAdmin(boolean admin)
  {
    this.admin = admin;
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
   * @param displayName
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
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
   * @param mail
   */
  public void setMail(String mail)
  {
    this.mail = mail;
  }

  /**
   * Method description
   *
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
   * @param password
   */
  public void setPassword(String password)
  {
    this.password = password;
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
  private boolean active = true;

  /** Field description */
  private boolean admin = false;

  /** Field description */
  private Long creationDate;

  /** Field description */
  private String displayName;

  /** Field description */
  private Long lastModified;

  /** Field description */
  private String mail;

  /** Field description */
  private String name;

  /** Field description */
  private String password;

  /** Field description */
  private String type;
}
