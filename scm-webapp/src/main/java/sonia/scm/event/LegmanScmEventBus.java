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

import com.github.legman.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sebastian Sdorra
 */
public class LegmanScmEventBus extends ScmEventBus
{

  /** Field description */
  private static final String NAME = "ScmEventBus";

  /**
   * the logger for LegmanScmEventBus
   */
  private static final Logger logger =
    LoggerFactory.getLogger(LegmanScmEventBus.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public LegmanScmEventBus()
  {
    eventBus = new EventBus(NAME);
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
    eventBus.post(event);
  }

  /**
   * Registers a object to the eventbus.
   *
   * @param object object to register
   *
   * @see {@link #register(java.lang.Object)}
   */
  @Override
  public void register(Object object)
  {
    logger.debug("register {} to event bus", object);
    eventBus.register(object);
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

    try
    {
      eventBus.unregister(object);
    }
    catch (IllegalArgumentException ex)
    {
      logger.trace("object {} was not registered", object);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** event bus */
  private final EventBus eventBus;
}
