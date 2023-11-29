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

package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableSet;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.metrics.MonitoringSystem;
import sonia.scm.metrics.ScrapeTarget;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class MetricsResourceTest {

  private RestDispatcher dispatcher;

  @Mock
  private Subject subject;

  @BeforeEach
  void setUpDispatcher() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(new MetricsResource(ImmutableSet.of(
      new NoScrapeMonitoringSystem(),
      new ScrapeMonitoringSystem()
    )));
  }

  @BeforeEach
  void setUpSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldReturn404ForUnknownMonitoringSystem() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/metrics/unknown");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void shouldReturn404ForMonitoringSystemWithoutScrapeTarget() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/metrics/noscrape");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void shouldReturn403WithoutPermission() throws URISyntaxException {
    doThrow(new AuthorizationException("not permitted")).when(subject).checkPermission("metrics:read");
    MockHttpRequest request = MockHttpRequest.get("/v2/metrics/scrape");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  void shouldReturnMetrics() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/v2/metrics/scrape");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getOutputHeaders().getFirst("Content-Type").toString()).hasToString("text/plain;charset=UTF-8");
    assertThat(response.getContentAsString()).isEqualTo("hello");
  }

  private static class NoScrapeMonitoringSystem implements MonitoringSystem {

    @Override
    public String getName() {
      return "noscrape";
    }

    @Override
    public MeterRegistry getRegistry() {
      return new SimpleMeterRegistry();
    }
  }

  private static class ScrapeMonitoringSystem implements MonitoringSystem {

    @Override
    public String getName() {
      return "scrape";
    }

    @Override
    public MeterRegistry getRegistry() {
      return new SimpleMeterRegistry();
    }

    @Override
    public Optional<ScrapeTarget> getScrapeTarget() {
      return Optional.of(new HelloScrapeTarget());
    }
  }

  private static class HelloScrapeTarget implements ScrapeTarget {

    @Override
    public String getContentType() {
      return "text/plain";
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
      outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
    }
  }

}
