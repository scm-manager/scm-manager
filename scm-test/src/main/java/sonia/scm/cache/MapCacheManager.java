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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Maps;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
@SuppressWarnings("unchecked")
public class MapCacheManager
  implements CacheManager, org.apache.shiro.cache.CacheManager
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // do nothing
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  @Override
  public synchronized <K, V> MapCache<K, V> getCache(String name)
  {
    return (MapCache<K, V>) cacheMap.computeIfAbsent(name, k -> new MapCache<K, V>());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Map<String, MapCache> cacheMap = Maps.newHashMap();
}
