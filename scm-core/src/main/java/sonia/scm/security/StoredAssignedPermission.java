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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Permission object which is stored and assigned to a specific user or group.
 *
 * @since 1.31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assigned-permission")
public class StoredAssignedPermission extends AssignedPermission
{

  private static final long serialVersionUID = -4593919877023168090L;

  private String id;

  /**
   * Constructor is only visible for JAXB.
   */
  public StoredAssignedPermission() {}

  public StoredAssignedPermission(String id, AssignedPermission permission)
  {
    super(permission);
    this.id = id;

  }

  public String getId()
  {
    return id;
  }

}
