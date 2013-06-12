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


package sonia.scm.collect;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Sebastian Sdorra
 */
public class IterableQueueTest
{

  /**
   * Method description
   *
   */
  @Test(expected = IllegalStateException.class)
  public void testDuplicatedEndReached()
  {
    IterableQueue<String> queue = new IterableQueue<String>();

    queue.endReached();
    queue.endReached();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIterator()
  {
    IterableQueue<String> queue = new IterableQueue<String>();

    assertEquals(QueueIterator.class, queue.iterator().getClass());
    queue.endReached();
    assertNotEquals(QueueIterator.class, queue.iterator().getClass());
  }

  /**
   * Method description
   *
   *
   * @throws ExecutionException
   * @throws InterruptedException
   *
   * @throws Exception
   */
  @Test
  public void testMultiThreaded() throws Exception
  {
    testMultiThreaded(5, 10, false, 1000);
  }

  /**
   * Method description
   *
   *
   * @throws ExecutionException
   * @throws InterruptedException
   *
   * @throws Exception
   */
  @Test
  public void testMultiThreadedWithRandomSleep() throws Exception
  {
    testMultiThreaded(5, 10, true, 50);
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalStateException.class)
  public void testPushEndReached()
  {
    IterableQueue<String> queue = new IterableQueue<String>();

    queue.push("a");
    queue.endReached();
    queue.push("b");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSingleConsumer()
  {
    final IterableQueue<Integer> queue = new IterableQueue<Integer>();

    new Thread(new IntegerProducer(queue, false, 100)).start();
    assertResult(Lists.newArrayList(queue), 100);
  }

  /**
   * Method description
   *
   *
   * @param result
   * @param itemCount
   */
  private void assertResult(List<Integer> result, int itemCount)
  {
    assertNotNull(result);
    assertEquals(itemCount, result.size());

    for (int c = 0; c < itemCount; c++)
    {
      assertEquals(Integer.valueOf(c), result.get(c));
    }
  }

  /**
   * Method description
   *
   *
   * @param threads
   * @param consumer
   * @param randomSleep
   * @param itemCount
   *
   * @throws Exception
   */
  private void testMultiThreaded(int threads, int consumer,
    boolean randomSleep, int itemCount)
    throws Exception
  {
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    List<Future<List<Integer>>> futures = Lists.newArrayList();

    final IterableQueue<Integer> queue = new IterableQueue<Integer>();

    for (int i = 0; i < consumer; i++)
    {
      Future<List<Integer>> future =
        executor.submit(new CallableQueueCollector<Integer>(queue));

      futures.add(future);
    }

    new Thread(new IntegerProducer(queue, randomSleep, itemCount)).start();

    for (Future<List<Integer>> f : futures)
    {
      assertResult(f.get(), itemCount);
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/03/01
   * @author         Enter your name here...
   */
  private static class IntegerProducer implements Runnable
  {

    /**
     * Constructs ...
     *
     *
     * @param queue
     * @param randomSleep
     * @param itemCount
     */
    public IntegerProducer(IterableQueue<Integer> queue, boolean randomSleep,
      int itemCount)
    {
      this.queue = queue;
      this.randomSleep = randomSleep;
      this.itemCount = itemCount;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     */
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

    //~--- fields -------------------------------------------------------------

    /** Field description */
    IterableQueue<Integer> queue;

    /** Field description */
    private int itemCount;

    /** Field description */
    private boolean randomSleep;
  }
}
