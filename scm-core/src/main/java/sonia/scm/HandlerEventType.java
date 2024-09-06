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

package sonia.scm;

/**
 * Handler event type.
 *
 */
public enum HandlerEventType
{

  /**
   * After a new object is stored by a handler.
   */
  CREATE(true),

  /**
   * After an object is modified by a handler.
   */
  MODIFY(true),

  /**
   * After an object is removed by a handler.
   */
  DELETE(true),

  /**
   * Before a new object is stored by a handler.
   * @since 1.16
   */
  BEFORE_CREATE(false),

  /**
   * Before an object is modified by a handler.
   * @since 1.16
   */
  BEFORE_MODIFY(false),

  /**
   * Before an object is removed by a handler.
   * @since 1.16
   */
  BEFORE_DELETE(false);

  private HandlerEventType(boolean post)
  {
    this.post = post;
  }

  /**
   * Returns true if the event is fired after the action is occurred.
   *
   * @since 1.21
   */
  public boolean isPost()
  {
    return post;
  }

  /**
   * Returns true if the event is fired before the action is occurred.
   *
   * @since 1.21
   */
  public boolean isPre()
  {
    return !post;
  }

  private final boolean post;
}
