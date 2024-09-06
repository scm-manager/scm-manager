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
import sonia.scm.event.AbstractHandlerEvent;
import sonia.scm.event.Event;

/**
 * The UserEvent is fired if a user object changes.
 *
 * @since 1.23
 */
@Event
public class UserEvent extends AbstractHandlerEvent<User>
{

  /**
   * Constructs a new user event.
   *
   *
   * @param eventType event type
   * @param user changed user
   */
  public UserEvent(HandlerEventType eventType, User user)
  {
    super(eventType, user);
  }

  /**
   * Constructs a new user event.
   *
   *
   * @param eventType type of the event
   * @param user changed user
   * @param oldUser old user
   */
  public UserEvent(HandlerEventType eventType, User user, User oldUser)
  {
    super(eventType, user, oldUser);
  }
}
