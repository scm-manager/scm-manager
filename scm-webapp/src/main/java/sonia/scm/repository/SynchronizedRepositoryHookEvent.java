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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class SynchronizedRepositoryHookEvent implements RepositoryHookEvent
{

  /** Field description */
  private static final Object LOCK = new Object();

  /**
   * the logger for SynchronizedRepositoryHookEvent
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SynchronizedRepositoryHookEvent.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param wrappedEvent
   * @param preProcessorUtil
   */
  private SynchronizedRepositoryHookEvent(RepositoryHookEvent wrappedEvent,
    PreProcessorUtil preProcessorUtil)
  {
    this.wrappedEvent = wrappedEvent;
    this.preProcessorUtil = preProcessorUtil;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   * @param preProcessorUtil
   *
   * @return
   */
  public static SynchronizedRepositoryHookEvent wrap(RepositoryHookEvent event,
    PreProcessorUtil preProcessorUtil)
  {
    SynchronizedRepositoryHookEvent syncedEvent;

    if (event instanceof SynchronizedRepositoryHookEvent)
    {
      syncedEvent = (SynchronizedRepositoryHookEvent) event;
    }
    else
    {
      syncedEvent = new SynchronizedRepositoryHookEvent(event,
        preProcessorUtil);
    }

    return syncedEvent;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Changeset> getChangesets()
  {
    if (changesets == null)
    {
      synchronized (LOCK)
      {
        if (changesets == null)
        {
          changesets = wrappedEvent.getChangesets();

          if (changesets != null)
          {
            prepareChangesetsForReturn();
          }
        }
      }
    }

    return changesets;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Repository getRepository()
  {
    return wrappedEvent.getRepository();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryHookType getType()
  {
    return wrappedEvent.getType();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   */
  @Override
  public void setRepository(Repository repository)
  {
    wrappedEvent.setRepository(repository);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void prepareChangesetsForReturn()
  {
    Repository repository = getRepository();

    logger.debug("prepare changesets of repository {} for return to hook",
      repository.getName());

    for (Changeset c : changesets)
    {
      preProcessorUtil.prepareForReturn(repository, c);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private volatile Collection<Changeset> changesets;

  /** Field description */
  private PreProcessorUtil preProcessorUtil;

  /** Field description */
  private RepositoryHookEvent wrappedEvent;
}
