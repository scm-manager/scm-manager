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

/**
 * Base class for {@link RepositoryHookEvent} wrappers.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public class WrappedRepositoryHookEvent extends RepositoryHookEvent {

  /**
   * Constructs a new WrappedRepositoryHookEvent.
   *
   * @param wrappedEvent event to wrap
   */
  protected WrappedRepositoryHookEvent(RepositoryHookEvent wrappedEvent) {
    super(wrappedEvent.getContext(), wrappedEvent.getRepository(),
      wrappedEvent.getType());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns a wrapped instance of the {@link RepositoryHookEvent}-
   *
   * @param event event to wrap
   * @return wrapper
   */
  public static WrappedRepositoryHookEvent wrap(RepositoryHookEvent event) {
    WrappedRepositoryHookEvent wrappedEvent = null;

    switch (event.getType()) {
      case POST_RECEIVE:
        wrappedEvent = new PostReceiveRepositoryHookEvent(event);

        break;

      case PRE_RECEIVE:
        wrappedEvent = new PreReceiveRepositoryHookEvent(event);

        break;

      default:
        throw new IllegalArgumentException("unsupported hook event type");
    }

    return wrappedEvent;
  }
}
