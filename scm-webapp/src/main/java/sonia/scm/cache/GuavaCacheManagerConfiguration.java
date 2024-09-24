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

package sonia.scm.cache;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Collections;
import java.util.List;


@XmlRootElement(name = "caches")
@XmlAccessorType(XmlAccessType.FIELD)
public class GuavaCacheManagerConfiguration
{
  @XmlElement(name = "cache")
  private List<GuavaNamedCacheConfiguration> caches;

  @XmlElement(name = "defaultCache")
  private GuavaCacheConfiguration defaultCache;

  public GuavaCacheManagerConfiguration() {}

 
  public GuavaCacheManagerConfiguration(GuavaCacheConfiguration defaultCache,
    List<GuavaNamedCacheConfiguration> caches)
  {
    this.defaultCache = defaultCache;
    this.caches = caches;
  }


  
  @SuppressWarnings("unchecked")
  public List<GuavaNamedCacheConfiguration> getCaches()
  {
    if (caches == null)
    {
      caches = Collections.emptyList();
    }

    return caches;
  }

  
  public GuavaCacheConfiguration getDefaultCache()
  {
    return defaultCache;
  }

}
