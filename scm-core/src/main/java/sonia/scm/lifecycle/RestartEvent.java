/**
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

package sonia.scm.lifecycle;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.event.Event;

/**
 * This event can be used to force a restart of the webapp context. The restart
 * event is useful during plugin development, because we don't have to restart
 * the whole server, to see our changes. The restart event could also be used
 * to install or upgrade plugins.
 *
 * But the restart event should be used carefully, because the whole context
 * will be restarted and that process could take some time.
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
