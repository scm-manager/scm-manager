/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.group;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;

/**
 * Abstract base class for {@link GroupManager} implementations. This class
 * implements the listener methods of the {@link GroupManager} interface.
 *
 * @author Sebastian Sdorra
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
