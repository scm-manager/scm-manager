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
