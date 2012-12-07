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



package sonia.scm.event;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.ThrowingEventBus;

import org.apache.shiro.concurrent.SubjectAwareExecutorService;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.Executors;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public class ScmEventBus extends EventBus
{

  /** Field description */
  private static ScmEventBus instance = new ScmEventBus();

  //~--- constructors ---------------------------------------------------------

  /**
   *  Constructs ...
   *
   */
  private ScmEventBus()
  {
    eventBus = new ThrowingEventBus();
    asyncEventBus = new AsyncEventBus(
      new SubjectAwareExecutorService(Executors.newCachedThreadPool()));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static ScmEventBus getInstance()
  {
    return instance;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Override
  public void post(Object event)
  {
    asyncEventBus.post(event);
    eventBus.post(event);
  }

  /**
   * Method description
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
   * Method description
   *
   *
   * @param object
   * @param async
   */
  public void register(Object object, boolean async)
  {
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
   * Method description
   *
   *
   * @param object
   */
  @Override
  public void unregister(Object object)
  {
    asyncEventBus.unregister(object);
    eventBus.unregister(object);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private AsyncEventBus asyncEventBus;

  /** Field description */
  private EventBus eventBus;
}
