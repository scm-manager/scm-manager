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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;
import java.util.List;

/**
 * A iterable queue. The queue can have multiple parallel consumer 
 * {@link Iterator}s, which can iterate over all items of the queue until the 
 * end of the queue is reached. The end of the queue if reached, if a producer 
 * call the method {@link #endReached()} and the iterator has consumed all items
 * of the backend list. <strong>Warning: </strong> The queue iterator blocks 
 * forever if the producer never call {@link #endReached()}.
 *
 * @author Sebastian Sdorra
 *
 * @since 1.29
 * @param <T> type of the queued items
 */
public final class IterableQueue<T> implements Iterable<T>
{

  /**
   * the logger for IterableQueue
   */
  private static final Logger logger =
    LoggerFactory.getLogger(IterableQueue.class);

  //~--- constructors ---------------------------------------------------------

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

  //~--- methods --------------------------------------------------------------

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
   *
   *
   * @return current size
   */
  int size()
  {
    return list.size();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the item at the specified index in this queue.
   *
   *
   * @param index index of the item in the queue
   *
   * @return item at the current index
   */
  T get(int index)
  {
    return list.get(index);
  }

  /**
   * Returns true if the end of the queue is reached.
   *
   *
   * @return true if the end is reached
   */
  boolean isEndReached()
  {
    return endReached;
  }

  //~--- fields ---------------------------------------------------------------

  /** marker for the end of the queue */
  private boolean endReached = false;

  /** backend list of the queue */
  private List<T> list;
}
