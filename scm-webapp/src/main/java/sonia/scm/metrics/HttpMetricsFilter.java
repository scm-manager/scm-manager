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
