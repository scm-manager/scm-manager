/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.collect;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.concurrent.Callable;

/**
 * A {@link Callable} which consumes all items from a {@link QueueIterator} and 
 * returns a list with all items from the queue.
 *
 * @author Sebastian Sdorra
 * 
 * @since 1.29
 *
 * @param <T> type of the queued items
 */
public class CallableQueueCollector<T> implements Callable<List<T>>
{

  /**
   * the logger for CallableListComsumer
   */
  private static final Logger logger =
    LoggerFactory.getLogger(CallableQueueCollector.class);

  //~--- constructors ---------------------------------------------------------

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

  //~--- methods --------------------------------------------------------------

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
