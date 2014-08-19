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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.util.AssertUtil;

/**
 * Abstract base class for {@link RepositoryManager} implementations. This class
 * implements the listener and hook methods of the {@link RepositoryManager}
 * interface.
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractRepositoryManager implements RepositoryManager
{

  /**
   * Sends a {@link RepositoryHookEvent} to each registered
   * {@link RepositoryHook} and sends the {@link RepositoryHookEvent} to
   * the {@link ScmEventBus}.
   *
   * @param event event to be fired
   */
  @Override
  public void fireHookEvent(RepositoryHookEvent event)
  {
    AssertUtil.assertIsNotNull(event);
    AssertUtil.assertIsNotNull(event.getRepository());
    AssertUtil.assertIsNotNull(event.getType());

    // prepare the event
    event = prepareHookEvent(event);

    // post wrapped hook to event system
    ScmEventBus.getInstance().post(WrappedRepositoryHookEvent.wrap(event));
  }

  /**
   * Send a {@link RepositoryEvent} to the {@link ScmEventBus}.
   *
   * @param event type of change event
   * @param repository repository that has changed
   * @param oldRepository old repository
   */
  protected void fireEvent(HandlerEventType event, Repository repository,
    Repository oldRepository)
  {
    ScmEventBus.getInstance().post(new RepositoryEvent(event, repository,
      oldRepository));
  }

  /**
   * Send a {@link RepositoryEvent} to the {@link ScmEventBus}.
   *
   * @param event type of change event
   * @param repository repository that has changed
   */
  protected void fireEvent(HandlerEventType event, Repository repository)
  {
    ScmEventBus.getInstance().post(new RepositoryEvent(event, repository));
  }

  /**
   * Prepare a hook event before it is fired to the event system of SCM-Manager.
   *
   *
   * @param event hook event
   * @since 1.26
   *
   * @return
   */
  protected RepositoryHookEvent prepareHookEvent(RepositoryHookEvent event)
  {
    return event;
  }
}
