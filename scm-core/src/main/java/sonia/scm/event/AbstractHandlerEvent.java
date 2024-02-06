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

package sonia.scm.event;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.HandlerEventType;

/**
 * Abstract base class for {@link HandlerEvent}.
 *
 * @since 2.0.0
 *
 * @param <T>
 */
public class AbstractHandlerEvent<T> implements HandlerEvent<T>
{

  private final HandlerEventType eventType;

  /** changed item */
  private final T item;

  private final T oldItem;

  public AbstractHandlerEvent(HandlerEventType eventType, T item)
  {
    this(eventType, item, null);
  }

  public AbstractHandlerEvent(HandlerEventType eventType, T item, T oldItem)
  {
    this.eventType = eventType;
    this.item = item;
    this.oldItem = oldItem;
  }


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


  @Override
  public int hashCode()
  {
    return Objects.hashCode(eventType, item, oldItem);
  }


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


  /**
   * Returns the type of the event.
   */
  @Override
  public HandlerEventType getEventType()
  {
    return eventType;
  }

  /**
   * Returns changed item.
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

}
