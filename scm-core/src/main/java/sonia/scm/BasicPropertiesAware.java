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


import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.xml.XmlMapStringAdapter;

import java.io.Serializable;
import java.util.Map;

/**
 * Default implementation of {@link PropertiesAware} interface.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicPropertiesAware implements PropertiesAware, Serializable
{

  private static final long serialVersionUID = -536608122577385802L;

  /** map to hold the properties */
  @XmlJavaTypeAdapter(XmlMapStringAdapter.class)
  protected Map<String, String> properties;

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

    final BasicPropertiesAware other = (BasicPropertiesAware) obj;

    return Objects.equal(properties, other.properties);
  }


  @Override
  public int hashCode()
  {
    return Objects.hashCode(properties);
  }


  @Override
  public void removeProperty(String key)
  {
    getProperties().remove(key);
  }



  @Override
  public Map<String, String> getProperties()
  {
    if (properties == null)
    {
      properties = Maps.newHashMap();
    }

    return properties;
  }


  @Override
  public String getProperty(String key)
  {
    return getProperties().get(key);
  }



  @Override
  public void setProperties(Map<String, String> properties)
  {
    this.properties = properties;
  }


  @Override
  public void setProperty(String key, String value)
  {
    getProperties().put(key, value);
  }

}
