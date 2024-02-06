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

package sonia.scm.repository;


import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.util.AssertUtil;

/**
 * Abstract base class for {@link RepositoryManager} implementations. This class
 * implements the listener and hook methods of the {@link RepositoryManager}
 * interface.
 *
 */
public abstract class AbstractRepositoryManager implements RepositoryManager {

  /**
   * Sends the {@link RepositoryHookEvent} to the {@link ScmEventBus}.
   *
   * @param event event to be fired
   */
  @Override
  public void fireHookEvent(RepositoryHookEvent event) {
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
   * @param event         type of change event
   * @param repository    repository that has changed
   * @param oldRepository old repository
   */
  protected void fireEvent(HandlerEventType event, Repository repository,
                           Repository oldRepository) {
    ScmEventBus.getInstance().post(new RepositoryModificationEvent(event, repository,
      oldRepository));
  }

  /**
   * Send a {@link RepositoryEvent} to the {@link ScmEventBus}.
   *
   * @param event      type of change event
   * @param repository repository that has changed
   */
  protected void fireEvent(HandlerEventType event, Repository repository) {
    ScmEventBus.getInstance().post(new RepositoryEvent(event, repository));
  }

  /**
   * Prepare a hook event before it is fired to the event system of SCM-Manager.
   *
   * @param event hook event
   * @since 1.26
   */
  protected RepositoryHookEvent prepareHookEvent(RepositoryHookEvent event) {
    return event;
  }
}
