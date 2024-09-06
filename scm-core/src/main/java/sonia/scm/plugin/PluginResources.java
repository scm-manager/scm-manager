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

import java.util.Set;


@XmlAccessorType(XmlAccessType.NONE)
public class PluginResources
{
  @XmlElement(name = "script")
  private Set<String> scriptResources;

  @XmlElement(name = "stylesheet")
  private Set<String> stylesheetResources;

  PluginResources() {}

  public PluginResources(Set<String> scriptResources,
    Set<String> stylesheetResources)
  {
    this.scriptResources = scriptResources;
    this.stylesheetResources = stylesheetResources;
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

    final PluginResources other = (PluginResources) obj;

    return Objects.equal(scriptResources, other.scriptResources)
      && Objects.equal(stylesheetResources, other.stylesheetResources);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(scriptResources, stylesheetResources);
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("scriptResources", scriptResources)
                  .add("stylesheetResources", stylesheetResources)
                  .toString();
    //J+
  }


  
  public Set<String> getScriptResources()
  {
    return scriptResources;
  }

  
  public Set<String> getStylesheetResources()
  {
    return stylesheetResources;
  }

}
