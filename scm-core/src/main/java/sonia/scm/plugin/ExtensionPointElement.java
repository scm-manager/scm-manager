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

package sonia.scm.plugin;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @since 2.0.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "extension-point")
public final class ExtensionPointElement
{
  @XmlElement(name = "class")
  private Class<?> clazz;

  private String description;

  @XmlElement(name = "multi")
  private boolean multiple = true;

  private boolean autoBind = true;

  ExtensionPointElement() {}

  public ExtensionPointElement(Class<?> clazz, String description,
    boolean multiple, boolean autoBind)
  {
    this.clazz = clazz;
    this.description = description;
    this.multiple = multiple;
    this.autoBind = autoBind;
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

    final ExtensionPointElement other = (ExtensionPointElement) obj;

    return Objects.equal(clazz, other.clazz)
      && Objects.equal(description, other.description)
      && Objects.equal(multiple, other.multiple)
      && Objects.equal(autoBind, other.autoBind);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(clazz, description, multiple, autoBind);
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("class", clazz)
                  .add("description", description)
                  .add("multiple", multiple)
                  .add("autoBind", autoBind)
                  .toString();
    //J+
  }


  
  public Class<?> getClazz()
  {
    return clazz;
  }

  
  public String getDescription()
  {
    return description;
  }

  
  public boolean isAutoBind()
  {
    return autoBind;
  }

  
  public boolean isMultiple()
  {
    return multiple;
  }

}
