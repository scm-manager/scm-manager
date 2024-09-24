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
