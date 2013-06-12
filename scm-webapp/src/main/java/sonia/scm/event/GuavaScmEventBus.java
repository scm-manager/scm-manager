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


package sonia.scm.event;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.ThrowingEventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.shiro.concurrent.SubjectAwareExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Sebastian Sdorra
 */
public class GuavaScmEventBus extends ScmEventBus
{

  /** Field description */
  private static final String THREAD_NAME = "EventBus-%s";

  /**
   * the logger for GuavaScmEventBus
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaScmEventBus.class);

  //~--- constructors ---------------------------------------------------------

  /**
   *  Constructs a new ScmEventBus
   *
   */
  public GuavaScmEventBus()
  {
    eventBus = new ThrowingEventBus();

    //J-
    ThreadFactory factory = new ThreadFactoryBuilder()
      .setNameFormat(THREAD_NAME).build();
    asyncEventBus = new AsyncEventBus(
      new SubjectAwareExecutorService(Executors.newCachedThreadPool(factory))
    );
    //J+
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param event
   */
  @Override
  public void post(Object event)
  {
    logger.debug("post {} to event bus", event);
    asyncEventBus.post(event);
    eventBus.post(event);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param object
   */
  @Override
  public void register(Object object)
  {
    register(object, true);
  }

  /**
   * Registers a object to the eventbus.
   *
   * @param object object to register
   * @param async handle event asynchronously
   *
   * @see {@link #register(java.lang.Object)}
   */
  @Override
  public void register(Object object, boolean async)
  {
    logger.debug("register {} to event bus, async = {}", object, async);

    if (async)
    {
      asyncEventBus.register(object);
    }
    else
    {
      eventBus.register(object);
    }
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param object
   */
  @Override
  public void unregister(Object object)
  {
    logger.debug("unregister {} from event bus", object);
    unregister(asyncEventBus, object);
    unregister(eventBus, object);
  }

  /**
   * Unregisters the object from eventbus.
   *
   *
   * @param bus event bus
   * @param object object to unregister
   */
  private void unregister(EventBus bus, Object object)
  {
    try
    {
      bus.unregister(object);
    }
    catch (IllegalArgumentException ex)
    {
      logger.trace("object {} was not registered at {}", object, bus);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** asynchronous event bus */
  private AsyncEventBus asyncEventBus;

  /** synchronous event bus */
  private EventBus eventBus;
}
