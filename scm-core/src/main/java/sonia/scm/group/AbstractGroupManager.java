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
import sonia.scm.event.ScmEventBus;

/**
 * Abstract base class for {@link GroupManager} implementations. This class
 * implements the listener methods of the {@link GroupManager} interface.
 *
 */
public abstract class AbstractGroupManager implements GroupManager
{

  /**
   * Creates a {@link GroupEvent} and send it to the {@link ScmEventBus}.
   *
   * @param event type of change event
   * @param group group that has changed
   */
  protected void fireEvent(HandlerEventType event, Group group)
  {
    fireEvent(new GroupEvent(event, group));
  }

  /**
   * Creates a {@link GroupModificationEvent} and send it to the {@link ScmEventBus}.
   *
   * @param event type of change event
   * @param group group that has changed
   * @param oldGroup old group
   */
  protected void fireEvent(HandlerEventType event, Group group, Group oldGroup)
  {
    fireEvent(new GroupModificationEvent(event, group, oldGroup));
  }
  
  /**
   * Sends a {@link GroupEvent} to the {@link ScmEventBus}.
   *
   * @param event group event
   */
  protected void fireEvent(GroupEvent event)
  {
    ScmEventBus.getInstance().post(event);
  }
}
