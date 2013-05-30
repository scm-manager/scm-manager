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

import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <C>
 */
public abstract class CacheManagerTestBase<C extends Cache>
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract CacheManager createCacheManager();

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @After
  public void tearDown() throws IOException
  {
    cacheManager.close();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSameReference()
  {
    Cache<String, String> c1 = cacheManager.getCache(String.class,
                                 String.class, "test-1");
    Cache<String, String> c2 = cacheManager.getCache(String.class,
                                 String.class, "test-1");

    assertIsSame(c1, c2);
  }

  /**
   * Method description
   *
   *
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Test
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    cacheManager = createCacheManager();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param c1
   * @param c2
   */
  protected void assertIsSame(Cache<String, String> c1,
    Cache<String, String> c2)
  {
    assertSame(c1, c2);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/03/25
   * @author         Enter your name here...
   */
  private static class AcquireCacheCallable implements Callable<Cache>
  {

    /**
     * Constructs ...
     *
     *
     * @param cacheManager
     * @param name
     */
    public AcquireCacheCallable(CacheManager cacheManager, String name)
    {
      this.cacheManager = cacheManager;
      this.name = name;
    }

    //~--- methods ------------------------------------------------------------

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
      return cacheManager.getCache(String.class, String.class, name);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private CacheManager cacheManager;

    /** Field description */
    private String name;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CacheManager cacheManager;
}
