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
import sonia.scm.ModificationHandlerEvent;
import sonia.scm.event.Event;

/**
 * Event which is fired whenever a repository is modified.
 *
 * @since 1.48
 */
@Event
public final class RepositoryModificationEvent extends RepositoryEvent implements ModificationHandlerEvent<Repository>
{
  
  private final Repository itemBeforeModification;
  
  /**
   * Constructs a new {@link RepositoryModificationEvent}.
   * 
   * @param eventType event type
   * @param item changed repository
   * @param itemBeforeModification repository before it was modified
   */
  public RepositoryModificationEvent(HandlerEventType eventType, Repository item, Repository itemBeforeModification)
  {
    super(eventType, item, itemBeforeModification);
    this.itemBeforeModification = itemBeforeModification;
  }

  @Override
  public Repository getItemBeforeModification()
  {
    return itemBeforeModification;
  }
  
}
