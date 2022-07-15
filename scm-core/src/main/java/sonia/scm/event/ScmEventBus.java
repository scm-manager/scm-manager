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

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.EventBus;
import com.github.legman.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.ServiceUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.ServiceLoader;

/**
 * Dispatches events to listeners, and provides ways for listeners to register
 * themselves. The ScmEventBus searches its implementation with the
 * {@link ServiceLoader}.
 *
 * @author Sebastian Sdorra
 * @apiviz.landmark
 * @see EventBus
 * @since 1.23
 */
public abstract class ScmEventBus {

  /**
   * Field description
   */
  private static ScmEventBus instance;

  /**
   * the logger for ScmEventBus
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmEventBus.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the singleton instance of the ScmEventBus
   *
   * @return singleton instance
   */
  public static ScmEventBus getInstance() {
    synchronized (ScmEventBus.class) {
      if (instance == null) {
        instance = ServiceUtil.getService(ScmEventBus.class);

        if (instance == null) {
          throw new IllegalStateException(
            "could not find a event bus implementation");
        } else {
          logger.info("use {} as event bus implementation",
            instance.getClass().getName());
        }
      }
    }

    return instance;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Post a event through the event bus. All registered subscribers will be
   * notified by the event bus.
   *
   * @param event event to send through the event bus
   */
  public abstract void post(Object event);

  /**
   * Register all handler methods with the {@link Subscribe} annotation as
   * subscriber for the event bus.
   *
   * @param subscriber subscriber object
   */
  public abstract void register(Object subscriber);

  /**
   * Unregister the given subscriber object from the event bus.
   *
   * @param subscriber subscriber object to unregister
   */
  public abstract void unregister(Object subscriber);
}
