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



package com.google.common.eventbus;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Throwables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.event.EventBusException;

//~--- JDK imports ------------------------------------------------------------

import java.lang.reflect.InvocationTargetException;

/**
 * {@link EventBus} which is able to throw {@link EventBusException}.
 *
 * @author Sebastian Sdorra
 */
public class ThrowingEventBus extends EventBus
{

  /**
   * the logger for ThrowingEventBus
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ThrowingEventBus.class);

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param event
   * @param wrapper
   */
  @Override
  void dispatch(Object event, EventHandler wrapper)
  {
    try
    {
      wrapper.handleEvent(event);
    }
    catch (InvocationTargetException ex)
    {
      Throwable cause = ex.getCause();

      Throwables.propagateIfPossible(cause);
      logger.trace("could not propagate exception, throw as EventBusException");

      throw new EventBusException(
        "could not handle event ".concat(event.toString()), cause);
    }
  }
}
