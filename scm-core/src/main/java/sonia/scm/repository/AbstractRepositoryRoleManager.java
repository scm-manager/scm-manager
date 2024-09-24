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

/**
 * Abstract base class for {@link RepositoryRoleManager} implementations. This class
 * implements the listener methods of the {@link RepositoryRoleManager} interface.
 */
public abstract class AbstractRepositoryRoleManager implements RepositoryRoleManager {

  /**
   * Send a {@link RepositoryRoleEvent} to the {@link ScmEventBus}.
   *
   * @param event type of change event
   * @param repositoryRole repositoryRole that has changed
   * @param oldRepositoryRole old repositoryRole
   */
  protected void fireEvent(HandlerEventType event, RepositoryRole repositoryRole, RepositoryRole oldRepositoryRole)
  {
    fireEvent(new RepositoryRoleModificationEvent(event, repositoryRole, oldRepositoryRole));
  }

  /**
   * Creates a new {@link RepositoryRoleEvent} and calls {@link #fireEvent(RepositoryRoleEvent)}.
   *
   * @param repositoryRole repositoryRole that has changed
   * @param event type of change event
   */
  protected void fireEvent(HandlerEventType event, RepositoryRole repositoryRole)
  {
    fireEvent(new RepositoryRoleEvent(event, repositoryRole));
  }

  /**
   * Send a {@link RepositoryRoleEvent} to the {@link ScmEventBus}.
   *
   * @param event repositoryRole event
   * @since 1.48
   */  
  protected void fireEvent(RepositoryRoleEvent event)
  {
    ScmEventBus.getInstance().post(event);
  }
}
