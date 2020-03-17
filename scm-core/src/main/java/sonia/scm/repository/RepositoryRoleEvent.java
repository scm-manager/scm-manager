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


package sonia.scm.repository;

import sonia.scm.HandlerEventType;
import sonia.scm.event.AbstractHandlerEvent;
import sonia.scm.event.Event;

/**
 * The RepositoryRoleEvent is fired if a repository role object changes.
 * @since 2.0
 */
@Event
public class RepositoryRoleEvent extends AbstractHandlerEvent<RepositoryRole> {

  /**
   * Constructs a new repositoryRole event.
   *
   *
   * @param eventType event type
   * @param repositoryRole changed repositoryRole
   */
  public RepositoryRoleEvent(HandlerEventType eventType, RepositoryRole repositoryRole) {
    super(eventType, repositoryRole);
  }

  /**
   * Constructs a new repositoryRole event.
   *
   *
   * @param eventType type of the event
   * @param repositoryRole changed repositoryRole
   * @param oldRepositoryRole old repositoryRole
   */
  public RepositoryRoleEvent(HandlerEventType eventType, RepositoryRole repositoryRole, RepositoryRole oldRepositoryRole) {
    super(eventType, repositoryRole, oldRepositoryRole);
  }
}
