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


import sonia.scm.event.Event;

/**
 * Pre receive repository hook events are fired before a change is written
 * through the repository. This class is wrapper of {@link RepositoryHookEvent}
 * for the event system of SCM-Manager.
 *
 * @since 1.23
 */
@Event
public final class PreReceiveRepositoryHookEvent
  extends WrappedRepositoryHookEvent
{
  public PreReceiveRepositoryHookEvent(RepositoryHookEvent wrappedEvent)
  {
    super(wrappedEvent);
  }
}
