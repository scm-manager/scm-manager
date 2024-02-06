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


import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 *
 * @param <C>
 */
public abstract class CacheManagerTestBase<C extends Cache>
{

  
  protected abstract CacheManager createCacheManager();


  @After
  public void tearDown() throws IOException
  {
    cacheManager.close();
  }

   @Test
  public void testSameReference()
  {
    Cache<String, String> c1 = cacheManager.getCache("test-1");
    Cache<String, String> c2 = cacheManager.getCache("test-1");

    assertIsSame(c1, c2);
  }

  @Test
  public void shouldClearCache() {
    Cache<String, String> c1 = cacheManager.getCache("test-1");
    c1.put("key1", "value1");

    Cache<String, String> c2 = cacheManager.getCache("test-2");
    c2.put("key2", "value2");

    cacheManager.clearAllCaches();

    assertEquals(c1.size(), 0);
    assertEquals(c2.size(), 0);
  }

  /**
   * Method description
   *
   *
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testSameReferenceMultiThreaded()
    throws InterruptedException, ExecutionException
  {
    ExecutorService executor = Executors.newFixedThreadPool(2);

    Set<Future<Cache>> caches = Sets.newHashSet();

    for (int i = 0; i < 20; i++)
    {
      //J-
      caches.add(
        executor.submit(new AcquireCacheCallable(cacheManager, "test-2"))
      );
      //J+
    }

    executor.shutdown();

    Cache c = null;

    for (Future<Cache> f : caches)
    {
      Cache nc = f.get();

      if (c != null)
      {
        assertIsSame(c, nc);
      }

      c = nc;
    }
  }


   @Before
  public void setUp()
  {
    cacheManager = createCacheManager();
  }



  protected void assertIsSame(Cache<String, String> c1, Cache<String, String> c2)
  {
    assertSame(c1, c2);
  }




  private static class AcquireCacheCallable implements Callable<Cache>
  {

  
    public AcquireCacheCallable(CacheManager cacheManager, String name)
    {
      this.cacheManager = cacheManager;
      this.name = name;
    }

    

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    @Override
    public Cache call() throws Exception
    {
      return cacheManager.getCache(name);
    }

    //~--- fields -------------------------------------------------------------

      private final CacheManager cacheManager;

      private final String name;
  }


  //~--- fields ---------------------------------------------------------------

  private CacheManager cacheManager;
}
