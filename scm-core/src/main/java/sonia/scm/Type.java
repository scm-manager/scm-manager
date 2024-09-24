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

package sonia.scm;


import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

/**
 * Base class for all objects which supports different types.
 *
 */
@XmlRootElement
public class Type
{
  /** display name of the type */
  private String displayName;

  /** unique name of the type */
  private String name;

  /**
   * Constructs {@link Type} object.
   * This constructor is required for JAXB.
   *
   */
  public Type() {}

  /**
   * Constructs {@link Type} object
   *
   *
   * @param name
   * @param displayName
   */
  public Type(String name, String displayName)
  {
    AssertUtil.assertIsNotEmpty(name);
    this.name = name;

    if (Util.isNotEmpty(displayName))
    {
      this.displayName = displayName;
    }
    else
    {
      this.displayName = name;
    }
  }


  /**
   * Returns true if the given object is equals.
   *
   *
   * @param obj
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

    final Type other = (Type) obj;

    if ((this.name == null)
        ? (other.name != null)
        : !this.name.equals(other.name))
    {
      return false;
    }

    if ((this.displayName == null)
        ? (other.displayName != null)
        : !this.displayName.equals(other.displayName))
    {
      return false;
    }

    return true;
  }

  /**
   * Returns the hash code of the object.
   */
  @Override
  public int hashCode()
  {
    int hash = 5;

    hash = 37 * hash + ((this.name != null)
                        ? this.name.hashCode()
                        : 0);
    hash = 37 * hash + ((this.displayName != null)
                        ? this.displayName.hashCode()
                        : 0);

    return hash;
  }

  @Override
  public String toString()
  {
    StringBuilder out = new StringBuilder("Type{name=");

    out.append(name).append(", displayName=").append(displayName).append("}");

    return out.toString();
  }


  /**
   * Returns the display name of the type.
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Returns the unique name of the type.
   */
  public String getName()
  {
    return name;
  }


  /**
   * Setter for the display name of the type
   *
   *
   *
   * @param displayName
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * Setter for the type name
   *
   *
   * @param name of the type
   */
  public void setName(String name)
  {
    this.name = name;
  }

}
