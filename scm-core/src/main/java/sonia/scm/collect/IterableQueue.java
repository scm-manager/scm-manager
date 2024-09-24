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


import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * An iterable queue. The queue can have multiple parallel consumer
 * {@link Iterator}s, which can iterate over all items of the queue until the
 * end of the queue is reached. The end of the queue if reached, if a producer
 * call the method {@link #endReached()} and the iterator has consumed all items
 * of the backend list. <strong>Warning: </strong> The queue iterator blocks
 * forever if the producer never call {@link #endReached()}.
 *
 *
 * @since 1.29
 * @param <T> type of the queued items
 */
public final class IterableQueue<T> implements Iterable<T>
{
  private boolean endReached = false;

  /** backend list of the queue */
  private List<T> list;

  private static final Logger logger =
    LoggerFactory.getLogger(IterableQueue.class);


  /**
   * Constructs a new {@link IterableQueue} with the default implementation
   * of the backing list.
   *
   */
  public IterableQueue()
  {
    list = Lists.newArrayList();
  }

  /**
   * Constructs a new {@link IterableQueue} with the given backing list.
   *
   *
   * @param list backend list for the queue
   */
  public IterableQueue(List<T> list)
  {
    this.list = list;
  }


  /**
   * Mark that the end of the queue is reached and notify all consuming
   * iterators.
   *
   * @throws IllegalStateException if the end of the queue if already reached
   */
  public synchronized void endReached()
  {
    if (endReached)
    {
      throw new IllegalStateException(
        "the end of the queue is already reached");
    }
    endReached = true;
    notifyAll();
  }

  /**
   * Returns a new consuming iterator for the queue. The methods
   * {@link Iterator#hasNext()} and {@link Iterator#next()} of the
   * {@link Iterator} will block until the next item is pushed to the queue, if
   * the queue is empty and the end is not reached. The
   * {@link Iterator#remove()} method of the {@link Iterator} is not implemented
   * and will throw a {@link UnsupportedOperationException}.
   *
   *
   * @return new consuming iterator
   */
  @Override
  public Iterator<T> iterator()
  {
    Iterator<T> iterator;

    if (endReached)
    {
      logger.trace("create list iterator");
      iterator = Iterators.unmodifiableIterator(list.iterator());
    }
    else
    {
      logger.trace("create queue iterator");
      iterator = new QueueIterator<>(this);
    }

    return iterator;
  }

  /**
   * Push a new item to the queue and notify all consuming iterators.
   *
   * @throws IllegalStateException if the end of the queue is already reached
   * @param item item to push to the queue
   */
  public synchronized void push(T item)
  {
    if (endReached)
    {
      throw new IllegalStateException(
        "the end of the queue is already reached");
    }

    list.add(item);
    notifyAll();
  }

  /**
   * Returns the current size of the queue.
   */
  int size()
  {
    return list.size();
  }


  /**
   * Returns the item at the specified index in this queue.
   */
  T get(int index)
  {
    return list.get(index);
  }

  /**
   * Returns true if the end of the queue is reached.
   */
  boolean isEndReached()
  {
    return endReached;
  }

}
