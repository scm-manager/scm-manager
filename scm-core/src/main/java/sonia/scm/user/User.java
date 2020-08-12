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
    
package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.ReducedModelObject;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.security.Principal;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@StaticPermissions(
  value = "user",
  globalPermissions = {"create", "list", "autocomplete"},
  permissions = {"read", "modify", "delete", "changePassword", "changePublicKeys"},
  custom = true, customGlobal = true
)
@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
public class User extends BasicPropertiesAware implements Principal, ModelObject, PermissionObject, ReducedModelObject
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

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param displayName
   * @param mail
   */
  public User(String name, String displayName, String mail, String password, String type, boolean active)
  {
    this.name = name;
    this.displayName = displayName;
    this.mail = mail;
    this.password = password;
    this.type = type;
    this.active = active;
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
    return Objects.hashCode(name, displayName, mail, type, password,
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
    return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("displayName",displayName)
            .add("mail", mail)
            .add("password", pwd)
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
  @Override
  public boolean isValid()
  {
    return ValidationUtil.isNameValid(name) && Util.isNotEmpty(displayName)
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
