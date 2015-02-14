/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.boot;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Stage;
import sonia.scm.event.Event;

/**
 * This event can be used to force a restart of the webapp context. The restart
 * event is useful during plugin development, because we don't have to restart
 * the whole server, to see our changes. The restart event can only be used in
 * stage {@link Stage#DEVELOPMENT}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Event
public class RestartEvent
{

  /**
   * Constructs ...
   *
   *
   * @param cause
   * @param reason
   */
  public RestartEvent(Class<?> cause, String reason)
  {
    this.cause = cause;
    this.reason = reason;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * The class which has fired the restart event.
   *
   *
   * @return class which has fired the restart event
   */
  public Class<?> getCause()
  {
    return cause;
  }

  /**
   * Returns the reason for the restart.
   *
   *
   * @return reason for restart
   */
  public String getReason()
  {
    return reason;
  }

  //~--- fields ---------------------------------------------------------------

  /** cause of restart */
  private final Class<?> cause;

  /** reason for restart */
  private final String reason;
}
