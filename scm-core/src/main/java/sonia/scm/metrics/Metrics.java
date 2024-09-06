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

package sonia.scm.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

import java.util.Collections;
import java.util.concurrent.ExecutorService;

/**
 * Util methods to collect metrics from known apis.
 *
 * @since 2.16.0
 */
public final class Metrics {

  private Metrics() {
  }

  /**
   * Collect metrics from an {@link ExecutorService}.
   *
   * @param registry meter registry
   * @param executorService executor service to monitor
   * @param name name of executor service
   * @param type type of executor service e.g.: cached, fixed, etc.
   */
  public static void executor(MeterRegistry registry, ExecutorService executorService, String name, String type) {
    new ExecutorServiceMetrics(
      executorService,
      name,
      Collections.singleton(Tag.of("type", type))
    ).bindTo(registry);
  }

  /**
   * Collect metrics from an {@link sonia.scm.repository.work.WorkingCopy}.
   *
   * @param registry meter registry
   * @param type type of repository
   */
  public static Timer workingCopyTimer(MeterRegistry registry, String type) {
    return Timer.builder("scm.workingcopy.duration")
      .description("Duration of temporary working copy lifetime")
      .tags("type", type)
      .register(registry);
  }
}
