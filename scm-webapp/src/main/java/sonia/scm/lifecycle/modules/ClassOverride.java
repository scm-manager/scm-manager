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

package sonia.scm.lifecycle.modules;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import sonia.scm.Validateable;


@XmlAccessorType(XmlAccessType.FIELD)
public class ClassOverride implements Validateable
{
  private Class<?> bind;

  private Class<?> to;

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

    final ClassOverride other = (ClassOverride) obj;

    return Objects.equal(bind, other.bind) && Objects.equal(to, other.to);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(bind, to);
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("bind", bind)
            .add("to", to)
            .toString();
    //J+
  }


  
  public Class<?> getBind()
  {
    return bind;
  }

  
  public Class<?> getTo()
  {
    return to;
  }

  
  @Override
  public boolean isValid()
  {
    return (bind != null) && (to != null);
  }



  public void setBind(Class<?> bind)
  {
    this.bind = bind;
  }


  public void setTo(Class<?> to)
  {
    this.to = to;
  }

}
