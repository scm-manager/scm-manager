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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * A {@link Callable} which consumes all items from a {@link QueueIterator} and 
 * returns a list with all items from the queue.
 *
 * 
 * @since 1.29
 *
 * @param <T> type of the queued items
 */
public class CallableQueueCollector<T> implements Callable<List<T>>
{

  private static final Logger logger =
    LoggerFactory.getLogger(CallableQueueCollector.class);


  /**
   * Constructs a new {@link CallableQueueCollector} from the given 
   * {@link IterableQueue}.
   *
   * @param queue queue to collect
   */
  public CallableQueueCollector(IterableQueue<T> queue)
  {
    this.queue = queue;
  }


  /**
   * Creates a {@link List} from all items of the queue.
   *
   *
   * @return {@link List} from all items of the queue
   */
  @Override
  public List<T> call()
  {
    logger.trace("start collecting item from queue");

    return Lists.newArrayList(queue);
  }

  //~--- fields ---------------------------------------------------------------

  /** queue to collect items from */
  private IterableQueue<T> queue;
}
