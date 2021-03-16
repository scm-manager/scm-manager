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
   */
  public static Timer workingCopyTimer(MeterRegistry registry) {
    return Timer.builder("scm.workingCopy.duration")
      .description("Duration of temporary working copy lifetime")
      .register(registry);
  }
}
