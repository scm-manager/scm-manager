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
    
package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * Permission object which is assigned to a specific user or group.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assigned-permission")
public class AssignedPermission implements PermissionObject, Serializable
{

  /** serial version uid */
  private static final long serialVersionUID = -7411338422110323879L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructor is only visible for JAXB.
   *
   */
  public AssignedPermission() {}

  /**
   * Constructs a new AssignedPermission.
   *
   *
   * @param permission assigned permission
   */
  public AssignedPermission(AssignedPermission permission)
  {
    this.name = permission.name;
    this.groupPermission = permission.groupPermission;
    this.permission = permission.permission;
  }

  /**
   * Constructs a new AssingnedPermmission.
   *
   *
   * @param name name of the user
   * @param permission permission string
   */
  public AssignedPermission(String name, String permission)
  {
    this(name, new PermissionDescriptor(permission));
  }

  public AssignedPermission(String name, PermissionDescriptor permission)
  {
    this(name, false, permission);
  }

  /**
   * Constructs a new AssingnedPermmission.
   *
   *
   * @param name name of the user or group
   * @param groupPermission true if the permission should be assigned to a group
   * @param permission permission string
   */
  public AssignedPermission(String name, boolean groupPermission,
    String permission)
  {
    this(name, groupPermission, new PermissionDescriptor(permission));
  }

  public AssignedPermission(String name, boolean groupPermission,
    PermissionDescriptor permission)
  {
    this.name = name;
    this.groupPermission = groupPermission;
    this.permission = permission;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
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

    final AssignedPermission other = (AssignedPermission) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(groupPermission, other.groupPermission)
      && Objects.equal(permission, other.permission);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, groupPermission, permission);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("name", name)
                  .add("groupPermission", groupPermission)
                  .add("permission", permission)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the name of the user or group which the permission is assigned.
   *
   *
   * @return name of user or group
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * Returns the description of the permission.
   */
  public PermissionDescriptor getPermission()
  {
    return permission;
  }

  /**
   * Returns true if the permission is assigned to a group.
   *
   *
   * @return true if the permission is assigned to a group
   */
  @Override
  public boolean isGroupPermission()
  {
    return groupPermission;
  }

  //~--- fields ---------------------------------------------------------------

  /** group permission indicator */
  @XmlElement(name = "group-permission")
  private boolean groupPermission;

  /** name of the user or group */
  private String name;

  /** string representation of the permission */
  private PermissionDescriptor permission;
}
