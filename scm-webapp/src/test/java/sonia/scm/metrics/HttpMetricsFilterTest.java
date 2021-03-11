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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpMetricsFilterTest {

  private MeterRegistry registry;

  @BeforeEach
  void prepare() {
    registry = new SimpleMeterRegistry();
  }

  @Test
  void shouldIncrementCounter() throws IOException, ServletException {
    filter("GET", 200);
    assertThat(count()).isEqualTo(1.0);
  }

  @Test
  void shouldCollectRequestDuration() throws IOException, ServletException {
    filter("GET", 200);
    filter("GET", 200);
    filter("GET", 200);
    assertThat(duration()).isGreaterThan(0.0);
  }

  private double duration() {
    return registry.get(HttpMetricsFilter.METRIC_DURATION)
      .tags("method", "GET", "outcome", "SUCCESS", "status", "200", "category", "unknown")
      .timer()
      .max(TimeUnit.NANOSECONDS);
  }

  @Test
  void shouldCreateDifferentCounters() throws IOException, ServletException {
    filter("GET", 200);
    filter("GET", 200);
    filter("POST", 200);
    filter("POST", 404);

    assertThat(counter("GET", 200)).isEqualTo(2.0);
    assertThat(counter("POST", 200)).isEqualTo(1.0);
    assertThat(counter("POST", 404)).isEqualTo(1.0);
  }

  private double count() {
    return registry.get(HttpMetricsFilter.METRIC_COUNT)
      .tags("method", "GET", "outcome", "SUCCESS", "status", "200", "category", "unknown")
      .counter()
      .count();
  }

  private double counter(String method, int statusCode) {
    return registry.get(HttpMetricsFilter.METRIC_COUNT)
      .tags("method", method, "status", String.valueOf(statusCode))
      .counter()
      .count();
  }

  private void filter(String requestMethod, int responseStatus) throws IOException, ServletException {
    HttpServletRequest request = request(requestMethod);
    HttpServletResponse response = response(responseStatus);
    FilterChain chain = chain();
    filter(request, response, chain);
  }

  private void filter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    RequestCategoryDetector detector = mock(RequestCategoryDetector.class);
    when(detector.detect(request)).thenReturn(RequestCategory.UNKNOWN);
    HttpMetricsFilter filter = new HttpMetricsFilter(registry, detector);
    filter.doFilter(request, response, chain);
  }

  private FilterChain chain() {
    return mock(FilterChain.class);
  }

  private HttpServletRequest request(String method) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn(method);
    return request;
  }

  private HttpServletResponse response(int status) {
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getStatus()).thenReturn(status);
    return response;
  }

}
