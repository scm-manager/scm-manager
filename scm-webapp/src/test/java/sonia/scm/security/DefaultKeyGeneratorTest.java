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
    
package sonia.scm.security;


import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.*;

import static org.junit.Assert.*;


public class DefaultKeyGeneratorTest
{

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

   @Test
  public void testSimple()
  {
    DefaultKeyGenerator generator = new DefaultKeyGenerator();

    String key = generator.createKey();

    assertNotNull(key);
    assertTrue(key.length() > 0);
  }

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
