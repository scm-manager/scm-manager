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
