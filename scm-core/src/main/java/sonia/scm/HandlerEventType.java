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
