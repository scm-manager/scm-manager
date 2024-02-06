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
