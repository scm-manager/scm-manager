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

package sonia.scm.group;

import sonia.scm.HandlerEventType;
import sonia.scm.ModificationHandlerEvent;

/**
 * Event which is fired whenever a group is modified.
 * 
 * @since 1.48
 */
public class GroupModificationEvent extends GroupEvent implements ModificationHandlerEvent<Group>
{
  
  private final Group itemBeforeModification;

  /**
   * Constructs a new {@link GroupModificationEvent}.
   * 
   * @param eventType type of event
   * @param item changed group
   * @param itemBeforeModification changed group before it was modified
   */
  public GroupModificationEvent(HandlerEventType eventType, Group item, Group itemBeforeModification)
  {
    super(eventType, item);
    this.itemBeforeModification = itemBeforeModification;
  }

  @Override
  public Group getItemBeforeModification()
  {
    return itemBeforeModification;
  }
  
}
