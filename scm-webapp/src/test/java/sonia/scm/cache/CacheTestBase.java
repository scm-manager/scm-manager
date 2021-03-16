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

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.util.IOUtil;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class CacheTestBase {
  private Cache<String, String> cache;
  private CacheManager cm;

  protected abstract CacheManager createCacheManager();

  @Before
  public void before() {
    cm = createCacheManager();
    cache = cm.getCache("test");
  }

  @After
  public void after() {
    IOUtil.close(cm);
  }

  @Test
  public void testClear() {
    cache.put("test", "test123");
    cache.put("test-1", "test123");
    cache.clear();
    assertNull(cache.get("test"));
    assertNull(cache.get("test-1"));
  }

  @Test
  public void testContains() {
    cache.put("test", "test123");
    cache.put("test-1", "test123");
    assertTrue(cache.contains("test"));
    assertTrue(cache.contains("test-1"));
    assertFalse(cache.contains("test-2"));
  }

  @Test
  public void testOverride() {
    cache.put("test", "test123");

    String previous = cache.put("test", "test456");

    assertEquals("test123", previous);
    assertEquals("test456", cache.get("test"));
  }

  @Test
  public void testPutAndGet() {
    cache.put("test", "test123");
    assertEquals("test123", cache.get("test"));
  }

  @Test
  public void testRemove() {
    cache.put("test", "test123");
    assertEquals("test123", cache.get("test"));

    String previous = cache.remove("test");

    assertEquals("test123", previous);
    assertNull(cache.get("test"));
  }

  @Test
  public void testRemoveAll() {
    cache.put("test-1", "test123");
    cache.put("test-2", "test456");
    cache.put("a-1", "test123");
    cache.put("a-2", "test123");

    Iterable<String> previous = cache.removeAll(item -> item != null && item.startsWith("test"));

    assertThat(previous, containsInAnyOrder("test123", "test456"));
    assertNull(cache.get("test-1"));
    assertNull(cache.get("test-2"));
    assertNotNull(cache.get("a-1"));
    assertNotNull(cache.get("a-2"));
  }

  @Test
  public void testCacheStatistics() {
    CacheStatistics stats = cache.getStatistics();
    // skip test if implementation does not support stats
    Assume.assumeTrue(stats != null);
    assertEquals("test", stats.getName());
    assertEquals(0L, stats.getHitCount());
    assertEquals(0L, stats.getMissCount());
    cache.put("test-1", "test123");
    cache.put("test-2", "test456");
    cache.get("test-1");
    cache.get("test-1");
    cache.get("test-1");
    cache.get("test-3");
    cache.get("test-4");
    cache.get("test-5");
    // check that stats have not changed
    assertEquals(0L, stats.getHitCount());
    assertEquals(0L, stats.getMissCount());
    stats = cache.getStatistics();
    assertEquals(3L, stats.getHitCount());
    // We get 2 misses on inserting new cache keys and 3 misses on the actual "cache.get" for non-existent cache keys
    assertEquals(5L, stats.getMissCount());
    assertEquals(0.375d, stats.getHitRate(), 0.0d);
    assertEquals(0.625d, stats.getMissRate(), 0.0d);
  }

  @Test
  public void testSize() {
    assertEquals(0, cache.size());
    cache.put("test", "test123");
    assertEquals(1, cache.size());
    cache.put("test-1", "test123");
    assertEquals(2, cache.size());
    cache.remove("test");
    assertEquals(1, cache.size());
    cache.clear();
    assertEquals(0, cache.size());
  }


}
