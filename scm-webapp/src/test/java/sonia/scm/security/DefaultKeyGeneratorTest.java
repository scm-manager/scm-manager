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
