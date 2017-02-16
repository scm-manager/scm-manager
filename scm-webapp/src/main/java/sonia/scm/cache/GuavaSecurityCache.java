/**
 * Copyright (c) 2014, Sebastian Sdorra
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

import com.google.common.cache.Cache;
import java.util.Collection;
import java.util.Set;
import org.apache.shiro.cache.CacheException;

/**
 * Guava based implementation of {@link org.apache.shiro.cache.Cache}.
 * 
 * @author Sebastian Sdorra
 * 
 * @since 1.52
 * 
 * @param <K>
 * @param <V>
 */
public class GuavaSecurityCache<K, V> extends GuavaBaseCache<K, V> implements org.apache.shiro.cache.Cache<K, V> {

  GuavaSecurityCache(Cache<K, V> cache, CopyStrategy copyStrategy, String name) {
    super(cache, copyStrategy, name);
  }
  
  @Override
  public V put(K key, V value) throws CacheException {
    V previousValue = cache.getIfPresent(key);
    cache.put(key, value);
    return previousValue;
  }

  @Override
  public V remove(K key) throws CacheException {
    V previousValue = cache.getIfPresent(key);
    cache.invalidate(key);
    return previousValue;
  }

  @Override
  public int size() {
    return (int) cache.size();
  }

  @Override
  public Set<K> keys() {
    return cache.asMap().keySet();
  }

  @Override
  public Collection<V> values() {
    return cache.asMap().values();
  }
  
}
