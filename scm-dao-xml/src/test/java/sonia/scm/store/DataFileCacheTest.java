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

package sonia.scm.store;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cache.MapCache;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataFileCacheTest {

  @Nested
  class WithActivatedCache {

    private final MapCache<File, Object> backingCache = new MapCache<>();
    private final DataFileCache dataFileCache = new DataFileCache(backingCache, true);

    @Test
    void shouldReturnCachedData() {
      File file = new File("/some.string");
      backingCache.put(file, "some string");

      DataFileCache.DataFileCacheInstance instance = dataFileCache.instanceFor(String.class);

      Object result = instance.get(file, () -> {
        throw new RuntimeException("should not be read");
      });

      assertThat(result).isSameAs("some string");
    }

    @Test
    void shouldReadDataIfNotCached() {
      File file = new File("/some.string");

      DataFileCache.DataFileCacheInstance instance = dataFileCache.instanceFor(String.class);

      Object result = instance.get(file, () -> "some string");

      assertThat(result).isSameAs("some string");
      assertThat(backingCache.get(file)).isSameAs("some string");
    }

    @Test
    void shouldReadDataAnewIfOfDifferentType() {
      File file = new File("/some.string");
      backingCache.put(file, 42);

      DataFileCache.DataFileCacheInstance instance = dataFileCache.instanceFor(String.class);

      Object result = instance.get(file, () -> "some string");

      assertThat(result).isSameAs("some string");
      assertThat(backingCache.get(file)).isSameAs("some string");
    }

    @Test
    void shouldRemoveOutdatedDataIfOfDifferentType() {
      File file = new File("/some.string");
      backingCache.put(file, 42);

      DataFileCache.DataFileCacheInstance instance = dataFileCache.instanceFor(String.class);

      Object result = instance.get(file, () -> null);

      assertThat(result).isNull();
      assertThat(backingCache.get(file)).isNull();
    }

    @Test
    void shouldCacheNewData() {
      File file = new File("/some.string");

      DataFileCache.DataFileCacheInstance instance = dataFileCache.instanceFor(String.class);

      instance.put(file, "some string");

      assertThat(backingCache.get(file)).isSameAs("some string");
    }

    @Test
    void shouldRemoveDataFromCache() {
      File file = new File("/some.string");
      backingCache.put(file, "some string");

      DataFileCache.DataFileCacheInstance instance = dataFileCache.instanceFor(String.class);

      instance.remove(file);

      assertThat(backingCache.get(file)).isNull();
    }
  }

  @Nested
  class WithDeactivatedCache {

    private final DataFileCache dataFileCache = new DataFileCache(null, false);

    @Test
    void shouldReadData() {
      File file = new File("/some.string");

      DataFileCache.DataFileCacheInstance instance = dataFileCache.instanceFor(String.class);

      Object result = instance.get(file, () -> "some string");

      assertThat(result).isSameAs("some string");
    }
  }
}
