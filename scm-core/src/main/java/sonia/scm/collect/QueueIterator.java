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

//~--- JDK imports ------------------------------------------------------------

import com.google.common.collect.UnmodifiableIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * Iterator for the {@link IterableQueue}. The {@link QueueIterator} should 
 * only be created from the {@link IterableQueue} by calling the 
 * {@link IterableQueue#iterator()}.
 *
 * @author Sebastian Sdorra
 *
 * @since 1.29
 * @param <T> type of the queued items
 */
public final class QueueIterator<T> extends UnmodifiableIterator<T>
{

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

  //~--- methods --------------------------------------------------------------

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
    if ( ! hasNext() ){
      throw new NoSuchElementException("no more items in the queue");
    }
    return queue.get(index++);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} {@code true} if the queue has more items. 
   * This method will block until the next item is pushed to the queue, if the 
   * queue is empty and the end is not 
   * reached.
   *
   *
   * @return {@code true} {@code true} if the queue has more items
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


  //~--- fields ---------------------------------------------------------------

  /** queue for the iterator */
  private final IterableQueue<T> queue;

  /** current index */
  private int index = 0;
}
