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

import com.google.inject.util.Providers;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.http.Outcome;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpMetricsFilterTest {

  private MeterRegistry registry;

  @BeforeEach
  void setUpRegistry() {
    registry = new SimpleMeterRegistry();
  }

  @Test
  void shouldCollectMetrics() throws IOException, ServletException {
    filter("GET", HttpServletResponse.SC_OK);
    filter("GET", HttpServletResponse.SC_OK);

    Timer timer = timer("GET", HttpServletResponse.SC_OK, Outcome.SUCCESS);
    assertThat(timer.count()).isEqualTo(2);
  }

  @Test
  void shouldCollectDifferentMetrics() throws IOException, ServletException {
    filter("GET", HttpServletResponse.SC_OK);
    filter("POST", HttpServletResponse.SC_CREATED);
    filter("DELETE", HttpServletResponse.SC_NOT_FOUND);

    Timer ok = timer("GET", HttpServletResponse.SC_OK, Outcome.SUCCESS);
    Timer created = timer("POST", HttpServletResponse.SC_CREATED, Outcome.SUCCESS);
    Timer notFound = timer("DELETE", HttpServletResponse.SC_NOT_FOUND, Outcome.CLIENT_ERROR);

    assertThat(ok.count()).isEqualTo(1);
    assertThat(created.count()).isEqualTo(1);
    assertThat(notFound.count()).isEqualTo(1);
  }

  private Timer timer(String method, int status, Outcome outcome) {
    return registry.get(HttpMetricsFilter.METRIC_DURATION)
      .tags("category", "UNKNOWN", "method", method, "outcome", outcome.name(), "status", String.valueOf(status))
      .timer();
  }

  private void filter(String requestMethod, int responseStatus) throws IOException, ServletException {
    HttpServletRequest request = request(requestMethod);
    HttpServletResponse response = response(responseStatus);
    FilterChain chain = chain();
    filter(request, response, chain);
  }

  private void filter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException, IOException, ServletException {
    RequestCategoryDetector detector = mock(RequestCategoryDetector.class);
    when(detector.detect(request)).thenReturn(RequestCategory.UNKNOWN);
    HttpMetricsFilter filter = new HttpMetricsFilter(Providers.of(registry), detector);
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
