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
