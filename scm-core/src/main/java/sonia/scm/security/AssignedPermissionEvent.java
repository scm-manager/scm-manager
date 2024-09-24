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
import sonia.scm.HandlerEventType;
import sonia.scm.event.Event;

import java.io.Serializable;

/**
 * Event which is fired after a {@link StoredAssignedPermission} was added,
 * removed or changed.
 *
 * @since 1.31
 */
@Event
public final class AssignedPermissionEvent implements Serializable
{
  /** changed permission */
  private AssignedPermission permission;

  private HandlerEventType type;

  private static final long serialVersionUID = 706824497813169009L;

  public AssignedPermissionEvent(HandlerEventType type,
                                 AssignedPermission permission)
  {
    this.type = type;
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

    final AssignedPermissionEvent other =
      (AssignedPermissionEvent) obj;

    return Objects.equal(type, other.type)
      && Objects.equal(permission, other.permission);
  }

 
  @Override
  public int hashCode()
  {
    return Objects.hashCode(type, permission);
  }

 
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("type", type)
                  .add("permission", permission)
                  .toString();
    //J+
  }

  public HandlerEventType getEventType()
  {
    return type;
  }

  /**
   * Returns the changed permission object.
   */
  public AssignedPermission getPermission()
  {
    return permission;
  }

}
