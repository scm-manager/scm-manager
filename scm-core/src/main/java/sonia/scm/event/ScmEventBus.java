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


import com.github.legman.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.ServiceUtil;

import java.util.ServiceLoader;

/**
 * Dispatches events to listeners, and provides ways for listeners to register
 * themselves. The ScmEventBus searches its implementation with the
 * {@link ServiceLoader}.
 *
 * @apiviz.landmark
 * @see EventBus
 * @since 1.23
 */
public abstract class ScmEventBus {

  private static ScmEventBus instance;

  private static final Logger logger =
    LoggerFactory.getLogger(ScmEventBus.class);


  /**
   * Returns the singleton instance of the ScmEventBus
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


  /**
   * Post an event through the event bus. All registered subscribers will be
   * notified by the event bus.
   *
   * @param event event to send through the event bus
   */
  public abstract void post(Object event);

  /**
   * Register all handler methods with the {@link com.github.legman.Subscribe} annotation as
   * subscriber for the event bus.
   */
  public abstract void register(Object subscriber);

  /**
   * Unregister the given subscriber object from the event bus.
   */
  public abstract void unregister(Object subscriber);
}
