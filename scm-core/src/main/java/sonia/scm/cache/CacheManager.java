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

import java.io.Closeable;

/**
 * The {@link CacheManager} holds references to {@link Cache}
 * and manages their creation.
 * This class is a singleton which can be retrieved via injection.
 *
 */
public interface CacheManager extends Closeable {

  /**
   * Returns the cache with the specified types and name.
   * If the cache does not exist, a new cache is created.
   *
   * @param name - The name of the cache
   * @param <K> - The type of the keys for the cache
   * @param <V>  - The type of cache elements
   *
   * @return the cache with the specified types and name
   */
  <K, V> Cache<K, V> getCache(String name);

  /**
   * Clears (aka invalidates) all caches.
   * @since 2.48.0
   */
  void clearAllCaches();
}
