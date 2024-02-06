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


import com.google.common.collect.UnmodifiableIterator;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * Iterator for the {@link IterableQueue}. The {@link QueueIterator} should
 * only be created from the {@link IterableQueue} by calling the
 * {@link IterableQueue#iterator()}.
 *
 *
 * @since 1.29
 * @param <T> type of the queued items
 */
public final class QueueIterator<T> extends UnmodifiableIterator<T>
{
  private final IterableQueue<T> queue;

  private int index = 0;

  /**
   * Constructs a new {@link QueueIterator} for the given {@link IterableQueue}.
   *
   *
   * @param queue backend queue
   */
  QueueIterator(IterableQueue<T> queue)
  {
    this.queue = queue;
  }


  /**
   * Returns the next item in the queue. This method will block until the next
   * item is pushed to the queue, if the queue is empty and the end is not
   * reached.
   *
   * @throws NoSuchElementException if the iteration has no more elements
   *
   * @return the next item in the queue
   */
  @Override
  public T next()
  {
    if (!hasNext())
    {
      throw new NoSuchElementException("no more items in the queue");
    }

    return queue.get(index++);
  }


  /**
   * Returns {@code true} if the queue has more items.
   * This method will block until the next item is pushed to the queue, if the
   * queue is empty and the end is not reached.
   */
  @Override
  public boolean hasNext()
  {
    boolean result = false;

    if (index < queue.size())
    {
      result = true;
    }
    else if (!queue.isEndReached())
    {
      synchronized (queue)
      {
        try
        {
          queue.wait(TimeUnit.SECONDS.toMillis(10));
          result = index < queue.size();
        }
        catch (InterruptedException ex)
        {
          throw new RuntimeException(ex);
        }
      }
    }

    return result;
  }

}
