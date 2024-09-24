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


import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @since 1.17
 */
public class MapCache<K, V>
  implements Cache<K, V>, org.apache.shiro.cache.Cache<K, V>
{
  private final Map<K, V> map = Maps.newHashMap();

   @Override
  public void clear()
  {
    map.clear();
  }


  @Override
  public boolean contains(K key)
  {
    return map.containsKey(key);
  }

  
  @Override
  public Set<K> keys()
  {
    return Collections.unmodifiableSet(map.keySet());
  }


  @Override
  public V put(K key, V value)
  {
    return map.put(key, value);
  }


  @Override
  public V remove(K key)
  {
    return map.remove(key);
  }


  @Override
  public Iterable<V> removeAll(Predicate<K> filter)
  {
    Set<V> values = Sets.newHashSet();

    for (K key : map.keySet())
    {
      if (filter.apply(key))
      {
        values.add(remove(key));
      }
    }

    return values;
  }

  
  @Override
  public int size()
  {
    return map.size();
  }

  
  @Override
  public Collection<V> values()
  {
    return Collections.unmodifiableCollection(map.values());
  }



  @Override
  public V get(K key)
  {
    return map.get(key);
  }

  
  @Override
  public CacheStatistics getStatistics()
  {
    return null;
  }

}
