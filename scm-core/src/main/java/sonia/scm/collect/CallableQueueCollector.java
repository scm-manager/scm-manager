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
