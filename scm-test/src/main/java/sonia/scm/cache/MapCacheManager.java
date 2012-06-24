/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public class MapCacheManager implements CacheManager
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
   * @param key
   * @param value
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  @Override
  public <K, V> Cache<K, V> getCache(Class<K> key, Class<V> value, String name)
  {
    CacheKey cacheKey = new CacheKey(key, value, name);
    Cache<K, V> cache = cacheMap.get(cacheKey);

    if (cache == null)
    {
      cache = new MapCache<K, V>();
      cacheMap.put(cacheKey, cache);
    }

    return cache;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/06/24
   * @author         Enter your name here...
   */
  private static class CacheKey
  {

    /**
     * Constructs ...
     *
     *
     * @param key
     * @param value
     * @param name
     */
    public CacheKey(Class key, Class value, String name)
    {
      this.key = key;
      this.value = value;
      this.name = name;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
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

      final CacheKey other = (CacheKey) obj;

      return Objects.equal(key, other.key) && Objects.equal(value, other.value)
             && Objects.equal(name, other.name);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode()
    {
      return Objects.hashCode(key, value, name);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Class key;

    /** Field description */
    private String name;

    /** Field description */
    private Class value;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<CacheKey, Cache> cacheMap = Maps.newHashMap();
}
