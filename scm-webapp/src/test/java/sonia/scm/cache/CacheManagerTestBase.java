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
