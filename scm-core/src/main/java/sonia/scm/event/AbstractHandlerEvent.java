/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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



package sonia.scm.event;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import sonia.scm.HandlerEventType;

/**
 * Abstract base class for {@link HandlerEvent}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 *
 * @param <T>
 */
public class AbstractHandlerEvent<T> implements HandlerEvent<T>
{

  /**
   * Constructs ...
   *
   *
   * @param eventType
   * @param item
   */
  public AbstractHandlerEvent(HandlerEventType eventType, T item)
  {
    this(eventType, item, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param eventType
   * @param item
   * @param oldItem
   */
  public AbstractHandlerEvent(HandlerEventType eventType, T item, T oldItem)
  {
    this.eventType = eventType;
    this.item = item;
    this.oldItem = oldItem;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final AbstractHandlerEvent<?> other = (AbstractHandlerEvent<?>) obj;

    return Objects.equal(eventType, other.eventType)
      && Objects.equal(item, other.item)
      && Objects.equal(oldItem, other.oldItem);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(eventType, item, oldItem);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                      .add("eventType", eventType)
                      .add("item", item)
                      .add("oldItem", oldItem)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the type of the event.
   *
   *
   * @return type of the event
   */
  @Override
  public HandlerEventType getEventType()
  {
    return eventType;
  }

  /**
   * Returns changed item.
   *
   *
   * @return changed item
   */
  @Override
  public T getItem()
  {
    return item;
  }

  /**
   * Returns old item or null. This method will always return null expect of 
   * modification events.
   *
   *
   * @return old item or null
   */
  @Override
  public T getOldItem()
  {
    return oldItem;
  }

  //~--- fields ---------------------------------------------------------------

  /** event type */
  private final HandlerEventType eventType;

  /** changed item */
  private final T item;

  /** old item */
  private final T oldItem;
}
