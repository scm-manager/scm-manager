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

package sonia.scm.collect;


import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class IterableQueueTest
{

  @Test(expected = IllegalStateException.class)
  public void testDuplicatedEndReached()
  {
    IterableQueue<String> queue = new IterableQueue<>();

    queue.endReached();
    queue.endReached();
  }

  @Test
  public void testIterator()
  {
    IterableQueue<String> queue = new IterableQueue<>();

    assertEquals(QueueIterator.class, queue.iterator().getClass());
    queue.endReached();
    assertNotEquals(QueueIterator.class, queue.iterator().getClass());
  }

  @Test
  public void testMultiThreaded() throws Exception
  {
    testMultiThreaded(5, 10, false, 1000);
  }

  @Test
  public void testMultiThreadedWithRandomSleep() throws Exception
  {
    testMultiThreaded(5, 10, true, 50);
  }

  @Test(expected = IllegalStateException.class)
  public void testPushEndReached()
  {
    IterableQueue<String> queue = new IterableQueue<>();

    queue.push("a");
    queue.endReached();
    queue.push("b");
  }

  @Test
  public void testSingleConsumer()
  {
    final IterableQueue<Integer> queue = new IterableQueue<>();

    new Thread(new IntegerProducer(queue, false, 100)).start();
    assertResult(Lists.newArrayList(queue), 100);
  }


  private void assertResult(List<Integer> result, int itemCount)
  {
    assertNotNull(result);
    assertEquals(itemCount, result.size());

    for (int c = 0; c < itemCount; c++)
    {
      assertEquals(Integer.valueOf(c), result.get(c));
    }
  }

  private void testMultiThreaded(int threads, int consumer,
    boolean randomSleep, int itemCount)
    throws Exception
  {
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    List<Future<List<Integer>>> futures = Lists.newArrayList();

    final IterableQueue<Integer> queue = new IterableQueue<>();

    for (int i = 0; i < consumer; i++)
    {
      Future<List<Integer>> future =
        executor.submit(new CallableQueueCollector<>(queue));

      futures.add(future);
    }

    new Thread(new IntegerProducer(queue, randomSleep, itemCount)).start();

    for (Future<List<Integer>> f : futures)
    {
      assertResult(f.get(), itemCount);
    }
  }

  private static class IntegerProducer implements Runnable
  {

    public IntegerProducer(IterableQueue<Integer> queue, boolean randomSleep,
      int itemCount)
    {
      this.queue = queue;
      this.randomSleep = randomSleep;
      this.itemCount = itemCount;
    }

    @Override
    public void run()
    {
      Random r = new Random();

      for (int c = 0; c < itemCount; c++)
      {
        if (randomSleep)
        {
          try
          {
            Thread.sleep(r.nextInt(5));
          }
          catch (InterruptedException ex)
          {
            throw new RuntimeException("thread interrupted", ex);
          }
        }

        queue.push(c);
      }

      queue.endReached();
    }

    IterableQueue<Integer> queue;

    private int itemCount;

    private boolean randomSleep;
  }
}
