/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
