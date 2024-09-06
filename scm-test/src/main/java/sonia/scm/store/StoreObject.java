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

package sonia.scm.store;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StoreObject
{

  public StoreObject() {}

 
  public StoreObject(String value)
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

    final StoreObject other = (StoreObject) obj;

    if ((this.value == null)
      ? (other.value != null)
      : !this.value.equals(other.value))
    {
      return false;
    }

    return true;
  }

  
  @Override
  public int hashCode()
  {
    int hash = 3;

    hash = 23 * hash + ((this.value != null)
      ? this.value.hashCode()
      : 0);

    return hash;
  }


  
  public String getValue()
  {
    return value;
  }

  //~--- fields ---------------------------------------------------------------

  private String value;
}
