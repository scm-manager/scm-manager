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
import com.github.legman.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.ServiceUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.ServiceLoader;

/**
 * Dispatches events to listeners, and provides ways for listeners to register
 * themselves. The ScmEventBus searches its implementation with the
 * {@link ServiceLoader}.
 *
 * @see EventBus
 * @author Sebastian Sdorra
 * @since 1.23
 *
 * @apiviz.landmark
 */
public abstract class ScmEventBus
{

  /** Field description */
  private volatile static ScmEventBus instance;

  /**
   * the logger for ScmEventBus
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmEventBus.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the singleton instance of the ScmEventBus
   *
   *
   * @return singleton instance
   */
  public static ScmEventBus getInstance()
  {
    if (instance == null)
    {
      synchronized (ScmEventBus.class)
      {
        if (instance == null)
        {
          instance = ServiceUtil.getService(ScmEventBus.class);

          if (instance == null)
          {
            throw new IllegalStateException(
              "could not find a event bus implementation");
          }
          else
          {
            logger.info("use {} as event bus implementation",
              instance.getClass().getName());
          }
        }
      }
    }

    return instance;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Post a event through the event bus. All registered subscribers will be
   * notified by the event bus.
   *
   * @param event event to send through the event bus
   */
  public abstract void post(Object event);

  /**
   * Register all handler methods with the {@link Subscribe} annotation as
   * subscriber for the event bus.
   *
   * @param subscriber subscriber object
   */
  public abstract void register(Object subscriber);

  /**
   * Unregister the given subscriber object from the event bus.
   *
   * @param subscriber subscriber object to unregister
   */
  public abstract void unregister(Object subscriber);
}
