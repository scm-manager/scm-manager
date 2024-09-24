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
import sonia.scm.event.AbstractHandlerEvent;
import sonia.scm.event.Event;

/**
 * The RepositoryRoleEvent is fired if a repository role object changes.
 * @since 2.0
 */
@Event
public class RepositoryRoleEvent extends AbstractHandlerEvent<RepositoryRole> {

  /**
   * Constructs a new repositoryRole event.
   *
   *
   * @param eventType event type
   * @param repositoryRole changed repositoryRole
   */
  public RepositoryRoleEvent(HandlerEventType eventType, RepositoryRole repositoryRole) {
    super(eventType, repositoryRole);
  }

  /**
   * Constructs a new repositoryRole event.
   *
   *
   * @param eventType type of the event
   * @param repositoryRole changed repositoryRole
   * @param oldRepositoryRole old repositoryRole
   */
  public RepositoryRoleEvent(HandlerEventType eventType, RepositoryRole repositoryRole, RepositoryRole oldRepositoryRole) {
    super(eventType, repositoryRole, oldRepositoryRole);
  }
}
