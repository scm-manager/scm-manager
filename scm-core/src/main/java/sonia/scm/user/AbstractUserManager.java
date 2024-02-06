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
