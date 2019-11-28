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


package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultKeyGeneratorTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testCreateRandom()
  {
    DefaultKeyGenerator gen = new DefaultKeyGenerator();

    for (int i = 0; i < 10000; i++)
    {
      int r = gen.createRandom();

      assertTrue(r >= 100);
      assertTrue(r <= 999);
    }
  }

  /**
   * Method description
   *
   *
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws TimeoutException
   */
  @Test
  public void testMultiThreaded()
    throws InterruptedException, ExecutionException, TimeoutException
  {
    final DefaultKeyGenerator generator = new DefaultKeyGenerator();

    ExecutorService executor = Executors.newFixedThreadPool(30);

    Set<Future<Set<String>>> futureSet = Sets.newHashSet();

    for (int i = 0; i < 10; i++)
    {
      Future<Set<String>> future = executor.submit(() -> {
        Set<String> keys = Sets.newHashSet();

        for (int i1 = 0; i1 < 1000; i1++)
        {
          String key = generator.createKey();

          if (keys.contains(key))
          {
            fail("dublicate key");
          }

          keys.add(key);
        }

        return keys;
      });

      futureSet.add(future);
    }

    executor.shutdown();

    Set<String> keys = Sets.newHashSet();

    for (Future<Set<String>> future : futureSet)
    {
      Set<String> futureKeys = future.get(20, TimeUnit.SECONDS);

      assertNotNull(futureKeys);
      assertEquals(1000, futureKeys.size());

      for (String key : futureKeys)
      {
        if (keys.contains(key))
        {
          fail("dublicate key");
        }

        keys.add(key);
      }
    }

    assertEquals(10000, keys.size());

  }

  /**
   * Method description
   *
   */
  @Test
  public void testSimple()
  {
    DefaultKeyGenerator generator = new DefaultKeyGenerator();

    String key = generator.createKey();

    assertNotNull(key);
    assertTrue(key.length() > 0);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testUniqueness()
  {
    DefaultKeyGenerator generator = new DefaultKeyGenerator();
    Set<String> keys = Sets.newHashSet();

    for (int i = 0; i < 10000; i++)
    {
      String key = generator.createKey();

      if (keys.contains(key))
      {
        fail("dublicate key");
      }

      keys.add(key);
    }

    assertEquals(10000, keys.size());
  }
}
