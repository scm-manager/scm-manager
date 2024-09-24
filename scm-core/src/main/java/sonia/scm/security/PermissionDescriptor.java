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
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

/**
 * Descriptor for available permission objects.
 *
 * @since 1.31
 */
@XmlRootElement(name = "permission")
@XmlAccessorType(XmlAccessType.FIELD)
public class PermissionDescriptor implements Serializable
{
  private String value;

  private static final long serialVersionUID = -9141065458354047154L;


  /**
   * Constructor is only visible for JAXB.
   */
  public PermissionDescriptor() {}

  public PermissionDescriptor(String value)
  {
    this.value = value;
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

    final PermissionDescriptor other = (PermissionDescriptor) obj;

    return Objects.equal(value, other.value);
  }

 
  @Override
  public int hashCode()
  {
    return value == null? -1: value.hashCode();
  }

 
  @Override
  public String toString()
  {

    //J-
    return MoreObjects.toStringHelper(this)
                  .add("value", value)
                  .toString();

    //J+
  }


  /**
   * Returns the string representation of the permission.
   */
  public String getValue()
  {
    return value;
  }
}
