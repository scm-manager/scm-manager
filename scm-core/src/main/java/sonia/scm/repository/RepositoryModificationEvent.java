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
    
package sonia.scm.repository;

import sonia.scm.HandlerEventType;
import sonia.scm.ModificationHandlerEvent;
import sonia.scm.event.Event;

/**
 * Event which is fired whenever a repository is modified.
 *
 * @since 1.48
 */
@Event
public final class RepositoryModificationEvent extends RepositoryEvent implements ModificationHandlerEvent<Repository>
{
  
  private final Repository itemBeforeModification;
  
  /**
   * Constructs a new {@link RepositoryModificationEvent}.
   * 
   * @param eventType event type
   * @param item changed repository
   * @param itemBeforeModification repository before it was modified
   */
  public RepositoryModificationEvent(HandlerEventType eventType, Repository item, Repository itemBeforeModification)
  {
    super(eventType, item, itemBeforeModification);
    this.itemBeforeModification = itemBeforeModification;
  }

  @Override
  public Repository getItemBeforeModification()
  {
    return itemBeforeModification;
  }
  
}
