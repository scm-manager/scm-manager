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


import sonia.scm.HandlerEventType;

/**
 * Base class for handler events.
 *
 * @since 1.23
 *
 * @param <T>
 */
public interface HandlerEvent<T>
{

  public HandlerEventType getEventType();

  /**
   * Returns the item which has changed.
   */
  public T getItem();

  /**
   * Returns old item or null. This method returns always expect for
   * modification events.
   *
   *
   * @return old item or null
   *
   * @since 2.0.0
   */
  public T getOldItem();
}
