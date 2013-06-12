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


package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
    this.name = name;
    this.permission = permission;
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
    return Objects.toStringHelper(this)
                  .add("name", name)
                  .add("groupPermisison", groupPermission)
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
   * Returns the string representation of the permission.
   *
   *
   * @return string representation of the permission
   */
  public String getPermission()
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
  private String permission;
}
