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

package sonia.scm.store;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ConfigValue;

import java.io.File;
import java.util.function.Supplier;

@Singleton
public class DataFileCache {

  private static final String CACHE_NAME = "sonia.cache.dataFileCache";
  private static final Logger LOG = LoggerFactory.getLogger(DataFileCache.class);

  private static final NoDataFileCacheInstance NO_CACHE = new NoDataFileCacheInstance();

  private final Cache<File, Object> cache;
  private final boolean cacheEnabled;

  @Inject
  DataFileCache(
    @ConfigValue(key = "cache.dataFile.enabled", defaultValue = "true", description = "Enabled caching for all read files") Boolean cacheEnabled,
    CacheManager cacheManager
    ) {
    this(cacheManager.getCache(CACHE_NAME), cacheEnabled);
  }

  @VisibleForTesting
  DataFileCache(Cache<File, Object> cache, boolean cacheEnabled) {
    this.cache = cache;
    this.cacheEnabled = cacheEnabled;
  }

  DataFileCacheInstance instanceFor(Class<?> type) {
    if (cacheEnabled) {
      return new GCacheDataFileCacheInstance(type);
    } else {
      return NO_CACHE;
    }
  }

  @Override
  public String toString() {
      long size = cache.keys().stream().map(File::length).reduce(0L, Long::sum);
      return String.format("data file cache, %s entries, %s bytes cached", cache.size(), size);
  }

  interface DataFileCacheInstance {
    <T> void put(File file, T item);

    <T> T get(File file, Supplier<T> reader);

    void remove(File file);
  }

  private static class NoDataFileCacheInstance implements DataFileCacheInstance {

    @Override
    public <T> void put(File file, T item) {
      // nothing to do without cache
    }

    @Override
    public <T> T get(File file, Supplier<T> reader) {
      return reader.get();
    }

    @Override
    public void remove(File file) {
      // nothing to do
    }
  }

  private class GCacheDataFileCacheInstance implements DataFileCacheInstance {

    private final Class<?> type;

    GCacheDataFileCacheInstance(Class<?> type) {
      this.type = type;
    }

    public <T> void put(File file, T item) {
      LOG.trace("put '{}' in {}", file, DataFileCache.this);
      if (item != null) {
        cache.put(file, item);
      }
    }

    public <T> T get(File file, Supplier<T> reader) {
      LOG.trace("get of '{}' from {}", file, DataFileCache.this);
      File absoluteFile = file.getAbsoluteFile();
      if (cache.contains(absoluteFile)) {
        T t = (T) cache.get(absoluteFile);
        if (t == null || type.isAssignableFrom(t.getClass())) {
          return t;
        } else {
          LOG.info("discarding cached entry with wrong type (expected: {}, got {})", type, t.getClass());
          cache.remove(file);
        }
      }

      T item = reader.get();
      if (item != null) {
        cache.put(absoluteFile, item);
      }
      return item;
    }

    public void remove(File file) {
      LOG.trace("remove of '{}' from {}", file, DataFileCache.this);
      cache.remove(file);
    }
  }
}
