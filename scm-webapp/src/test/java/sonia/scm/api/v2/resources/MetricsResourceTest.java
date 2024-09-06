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
