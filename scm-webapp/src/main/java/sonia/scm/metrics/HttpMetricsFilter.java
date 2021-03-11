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
 *
 */

package sonia.scm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.DefaultHttpServletRequestTagsProvider;
import io.micrometer.core.instrument.binder.http.HttpServletRequestTagsProvider;
import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.web.filter.HttpFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebElement(Filters.PATTERN_ALL)
@Priority(Filters.PRIORITY_PRE_BASEURL)
public class HttpMetricsFilter extends HttpFilter {

  static final String METRIC_DURATION = "scm.http.request.duration";
  static final String METRIC_COUNT = "scm.http.request.count";

  private final HttpServletRequestTagsProvider tagsProvider = new DefaultHttpServletRequestTagsProvider();

  private final MeterRegistry registry;
  private final RequestCategoryDetector detector;

  @Inject
  public HttpMetricsFilter(MeterRegistry registry, RequestCategoryDetector detector) {
    this.registry = registry;
    this.detector = detector;
    Timer.builder(METRIC_DURATION)
      .description("Duration of an http request")
      .register(registry);
    Counter.builder(METRIC_COUNT)
      .description("Count of http requests")
      .register(registry);
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    Timer.Sample sample = Timer.start(registry);
    try {
      chain.doFilter(request, response);
    } finally {
      Tags tags = tags(request, response);
      sample.stop(registry.timer(METRIC_DURATION, tags));
      registry.counter(METRIC_COUNT, tags).increment();
    }
  }

  private Tags tags(HttpServletRequest request, HttpServletResponse response) {
    Iterable<Tag> tags = tagsProvider.getTags(request, response);
    return Tags.concat(tags, "category", detector.detect(request).asValue());
  }
}
