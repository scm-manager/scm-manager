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
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.DefaultHttpJakartaServletRequestTagsProvider;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.web.filter.HttpFilter;

import java.io.IOException;

@WebElement(Filters.PATTERN_ALL)
@Priority(Filters.PRIORITY_PRE_BASEURL)
public class HttpMetricsFilter extends HttpFilter {

  static final String METRIC_DURATION = "http.server.requests";

  private final DefaultHttpJakartaServletRequestTagsProvider tagsProvider = new DefaultHttpJakartaServletRequestTagsProvider();

  private final Provider<MeterRegistry> registryProvider;
  private final RequestCategoryDetector detector;

  @Inject
  public HttpMetricsFilter(Provider<MeterRegistry> registryProvider, RequestCategoryDetector detector) {
    this.registryProvider = registryProvider;
    this.detector = detector;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    MeterRegistry registry = registryProvider.get();
    Timer.Sample sample = Timer.start(registry);
    try {
      chain.doFilter(request, response);
    } finally {
      Tags tags = tags(request, response);
      sample.stop(timer(registry, tags));
    }
  }

  private Timer timer(MeterRegistry registry, Tags tags) {
    return Timer.builder(METRIC_DURATION)
      .description("Duration of an http request")
      .tags(tags)
      .register(registry);
  }

  private Tags tags(HttpServletRequest request, HttpServletResponse response) {
    Iterable<Tag> tags = tagsProvider.getTags(request, response);
    return Tags.concat(tags, "category", detector.detect(request).name());
  }
}
