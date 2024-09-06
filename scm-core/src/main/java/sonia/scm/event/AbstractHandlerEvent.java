/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
