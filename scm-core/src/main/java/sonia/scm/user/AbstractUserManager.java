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
import sonia.scm.event.ScmEventBus;

/**
 * Abstract base class for {@link UserManager} implementations. This class
 * implements the listener methods of the {@link UserManager} interface.
 *
 */
public abstract class AbstractUserManager implements UserManager
{

  /**
   * Send a {@link UserEvent} to the {@link ScmEventBus}.
   *
   * @param event type of change event
   * @param user user that has changed
   * @param oldUser old user
   */
  protected void fireEvent(HandlerEventType event, User user, User oldUser)
  {
    fireEvent(new UserModificationEvent(event, user, oldUser));
  }

  /**
   * Creates a new {@link UserEvent} and calls {@link #fireEvent(sonia.scm.user.UserEvent)}.
   *
   * @param user user that has changed
   * @param event type of change event
   */
  protected void fireEvent(HandlerEventType event, User user)
  {
    fireEvent(new UserEvent(event, user));
  }

  /**
   * Send a {@link UserEvent} to the {@link ScmEventBus}.
   *
   * @param event user event
   * @since 1.48
   */  
  protected void fireEvent(UserEvent event)
  {
    ScmEventBus.getInstance().post(event);
  }
}
