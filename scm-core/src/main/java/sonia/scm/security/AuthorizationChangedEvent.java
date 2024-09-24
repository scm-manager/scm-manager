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

package sonia.scm.security;

import sonia.scm.event.Event;

/**
 * This type of event is fired whenever an authorization relevant data changes. This event
 * is especially useful for cache invalidation.
 *
 * @since 1.52
 */
@Event
public final class AuthorizationChangedEvent {

  private final String nameOfAffectedUser;

  private AuthorizationChangedEvent(String nameOfAffectedUser) {
    this.nameOfAffectedUser = nameOfAffectedUser;
  }

  /**
   * Returns {@code true} if every user is affected by this data change.
   */
  public boolean isEveryUserAffected(){
    return nameOfAffectedUser == null;
  }

  /**
   * Returns the name of the user which is affected by this event.
   */
  public String getNameOfAffectedUser(){
    return nameOfAffectedUser;
  }

  /**
   * Creates a new event which affects every user.
   */
  public static AuthorizationChangedEvent createForEveryUser() {
    return new AuthorizationChangedEvent(null);
  }

  /**
   * Create a new event which affect a single user.
   */
  public static AuthorizationChangedEvent createForUser(String nameOfAffectedUser) {
    return new AuthorizationChangedEvent(nameOfAffectedUser);
  }

}
