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
import com.github.legman.micrometer.MicrometerPlugin;
import com.github.legman.shiro.ShiroPlugin;
import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.metrics.MeterRegistryProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Sebastian Sdorra
 */
public class LegmanScmEventBus extends ScmEventBus {

  private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();
  private static final String FORMAT_NAME = "ScmEventBus-%s";

  /**
   * the logger for LegmanScmEventBus
   */
  private static final Logger logger = LoggerFactory.getLogger(LegmanScmEventBus.class);

  private String name;
  private EventBus eventBus;

  public LegmanScmEventBus() {
    eventBus = create(null);
  }

  @VisibleForTesting
  LegmanScmEventBus(Executor executor) {
    eventBus = create(executor);
  }

  private EventBus create(Executor executor) {
    name = String.format(FORMAT_NAME, INSTANCE_COUNTER.incrementAndGet());
    logger.info("create new event bus {}", name);

    MicrometerPlugin micrometerPlugin = new MicrometerPlugin(MeterRegistryProvider.getStaticRegistry())
      .withExecutorTags(Tag.of("type", "fixed"));

    ShiroPlugin shiroPlugin = new ShiroPlugin();

    EventBus.Builder builder = EventBus.builder()
      .withIdentifier(name)
      .withPlugins(shiroPlugin, micrometerPlugin);

    if (executor != null) {
      builder.withExecutor(executor);
    }

    return builder.build();
  }

  @Override
  public void post(Object event) {
    if (eventBus != null) {
      logger.debug("post {} to event bus {}", event, name);
      eventBus.post(event);
    } else {
      logger.error("failed to post event {}, because event bus is shutdown", event);
    }
  }

  /**
   * Registers a object to the eventbus.
   *
   * @param object object to register
   */
  @Override
  public void register(Object object) {
    if (eventBus != null) {
      logger.trace("register {} to event bus {}", object, name);
      eventBus.register(object);
    } else {
      logger.error("failed to register {}, because eventbus is shutdown", object);
    }
  }

  @Override
  public void unregister(Object object) {
    if (eventBus != null) {
      logger.trace("unregister {} from event bus {}", object, name);

      try {
        eventBus.unregister(object);
      } catch (IllegalArgumentException ex) {
        logger.trace("object {} was not registered", object);
      }
    } else {
      logger.error("failed to unregister object {}, because event bus is shutdown", object);
    }
  }

  @Subscribe(async = false)
  public void shutdownEventBus(ShutdownEventBusEvent shutdownEventBusEvent) {
    if (eventBus != null) {
      logger.info("shutdown event bus executor for {}, because of received ShutdownEventBusEvent", name);
      eventBus.shutdown();
      eventBus = null;
    } else {
      logger.warn("event bus was already shutdown");
    }
  }

  @Subscribe(async = false)
  public void recreateEventBus(RecreateEventBusEvent recreateEventBusEvent) {
    if (eventBus != null) {
      logger.info("shutdown event bus executor for {}, because of received RecreateEventBusEvent", name);
      eventBus.shutdown();
    }
    logger.info("recreate event bus because of received RecreateEventBusEvent");
    eventBus = create(null);
  }

}
