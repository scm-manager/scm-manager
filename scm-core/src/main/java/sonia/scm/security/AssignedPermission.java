/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.security;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.auditlog.AuditLogEntity;

import java.io.Serializable;

/**
 * Permission object which is assigned to a specific user or group.
 *
 * @since 1.31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assigned-permission")
@AuditEntry(labels = "permission")
public class AssignedPermission implements PermissionObject, Serializable, AuditLogEntity
{

  private static final long serialVersionUID = -7411338422110323879L;

  /** group permission indicator */
  @XmlElement(name = "group-permission")
  private boolean groupPermission;

  /** name of the user or group */
  private String name;

  /** string representation of the permission */
  private PermissionDescriptor permission;

  /**
   * Constructor is only visible for JAXB.
   */
  public AssignedPermission() {}

  public AssignedPermission(AssignedPermission permission)
  {
    this.name = permission.name;
    this.groupPermission = permission.groupPermission;
    this.permission = permission.permission;
  }

  public AssignedPermission(String name, String permission)
  {
    this(name, new PermissionDescriptor(permission));
  }

  public AssignedPermission(String name, PermissionDescriptor permission)
  {
    this(name, false, permission);
  }

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

 
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, groupPermission, permission);
  }

 
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


  /**
   * Returns the name of the user or group which the permission is assigned.
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
   */
  @Override
  public boolean isGroupPermission()
  {
    return groupPermission;
  }

  @Override
  public String getEntityName() {
    return getName();
  }
}
