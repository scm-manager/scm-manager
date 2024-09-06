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

package sonia.scm.user;

import sonia.scm.HandlerEventType;
import sonia.scm.ModificationHandlerEvent;
import sonia.scm.event.Event;

/**
 * Event which is fired whenever a user is modified.
 *
 * @since 1.48
 */
@Event
public class UserModificationEvent extends UserEvent implements ModificationHandlerEvent<User>
{

  private final User itemBeforeModification;
  
  /**
   * Constructs a new {@link UserModificationEvent}.
   * 
   * @param eventType type of event
   * @param item changed user
   * @param itemBeforeModification changed user before it was modified
   */
  public UserModificationEvent(HandlerEventType eventType, User item, User itemBeforeModification)
  {
    super(eventType, item);
    this.itemBeforeModification = itemBeforeModification;
  }

  @Override
  public User getItemBeforeModification()
  {
    return itemBeforeModification;
  }
  
}
