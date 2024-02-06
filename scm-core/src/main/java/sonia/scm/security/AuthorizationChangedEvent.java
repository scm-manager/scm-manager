/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
