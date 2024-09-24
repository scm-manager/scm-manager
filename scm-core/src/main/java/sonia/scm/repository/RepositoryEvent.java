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
 * The RepositoryEvent is fired if a {@link Repository} object changes.
 *
 * @since 1.23
 */
@Event
public class RepositoryEvent extends AbstractHandlerEvent<Repository>
{
  public RepositoryEvent(HandlerEventType eventType, Repository repository)
  {
    super(eventType, repository);
  }

  public RepositoryEvent(HandlerEventType eventType, Repository repository,
    Repository oldRepository)
  {
    super(eventType, repository, oldRepository);
  }
}
