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


import com.google.common.collect.Maps;

import java.io.IOException;

import java.util.Map;

/**
 * @since 1.17
 */
@SuppressWarnings("unchecked")
public class MapCacheManager
  implements CacheManager, org.apache.shiro.cache.CacheManager
{
  private final Map<String, MapCache> cacheMap = Maps.newHashMap();

  @Override
  public void close() throws IOException
  {

    // do nothing
  }


  @Override
  public synchronized <K, V> MapCache<K, V> getCache(String name)
  {
    return (MapCache<K, V>) cacheMap.computeIfAbsent(name, k -> new MapCache<K, V>());
  }

  @Override
  public void clearAllCaches() {
    for(MapCache<?, ?> cache : cacheMap.values()) {
      cache.clear();
    }
  }

}
