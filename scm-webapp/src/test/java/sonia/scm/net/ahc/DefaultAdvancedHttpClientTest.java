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

package sonia.scm.net.ahc;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.util.HttpUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DefaultAdvancedHttpClientTest {

  private static final int TIMEOUT_CONNECTION = 30000;
  private static final int TIMEOUT_READ = 1200000;

  @Mock
  private HttpsURLConnection connection;

  @Mock
  private Tracer tracer;

  @Mock
  private Span span;

  @Mock
  private TrustManager trustManager;

  private Set<ContentTransformer> transformers;

  private ScmConfiguration configuration;

  private DefaultAdvancedHttpClient client;

  private Proxy proxy;

  @BeforeEach
  void setUp() {
    configuration = new ScmConfiguration();
    transformers = new HashSet<>();
    HttpURLConnectionFactory connectionFactory = new HttpURLConnectionFactory(
      new GlobalProxyConfiguration(configuration),
      Providers.of(trustManager),
      (url, proxy) -> {
        this.proxy = proxy;
        return connection;
      },
      () -> SSLContext.getInstance("TLS")
    );

    client = new DefaultAdvancedHttpClient(connectionFactory, tracer, transformers);
    lenient().when(tracer.span(anyString())).thenReturn(span);
  }

  @Test
  void shouldApplyBaseSettings() throws IOException {
    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).request();

    verify(connection).setRequestMethod(HttpMethod.GET);
    verify(connection).setReadTimeout(TIMEOUT_READ);
    verify(connection).setConnectTimeout(TIMEOUT_CONNECTION);
    verify(connection).addRequestProperty(HttpUtil.HEADER_CONTENT_LENGTH, "0");
  }

  @Test
  void shouldThrowContentTransformerNotFound(){
    assertThrows(ContentTransformerNotFoundException.class, () -> client.createTransformer(String.class, "text/plain"));
  }

  @Test
  void shouldCreateContentTransformer() {
    ContentTransformer transformer = mock(ContentTransformer.class);
    when(transformer.isResponsible(String.class, "text/plain")).thenReturn(Boolean.TRUE);
    transformers.add(transformer);
    ContentTransformer t = client.createTransformer(String.class, "text/plain");
    assertThat(t).isSameAs(transformer);
  }

  @Test
  void shouldApplyContent() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    when(connection.getOutputStream()).thenReturn(baos);

    AdvancedHttpRequestWithBody request = new AdvancedHttpRequestWithBody(
      client, HttpMethod.PUT, "https://www.scm-manager.org"
    );

    request.stringContent("test").request();
    verify(connection).setDoOutput(true);
    verify(connection).addRequestProperty(HttpUtil.HEADER_CONTENT_LENGTH, "4");
    assertThat(baos.toString("UTF-8")).isEqualTo("test");
  }

  @Test
  void shouldApplyHeaders() throws IOException {
    AdvancedHttpRequest request = new AdvancedHttpRequest(
      client, HttpMethod.POST, "http://www.scm-manager.org"
    );

    request.header("Header-One", "One").header("Header-Two", "Two").request();
    verify(connection).setRequestMethod(HttpMethod.POST);
    verify(connection).addRequestProperty("Header-One", "One");
    verify(connection).addRequestProperty("Header-Two", "Two");
  }

  @Test
  void shouldApplyMultipleHeaders() throws IOException {
    AdvancedHttpRequest request = new AdvancedHttpRequest(
      client, HttpMethod.POST, "http://www.scm-manager.org"
    );

    request.header("Header-One", "One").header("Header-One", "Two").request();
    verify(connection).setRequestMethod(HttpMethod.POST);
    verify(connection).addRequestProperty("Header-One", "One");
    verify(connection).addRequestProperty("Header-One", "Two");
  }

  @Test
  void shouldReturnRequestWithoutContent() throws IOException {
    AdvancedHttpRequestWithBody request = new AdvancedHttpRequestWithBody(
      client, HttpMethod.PUT, "https://www.scm-manager.org");

    request.request();
    verify(connection).addRequestProperty(HttpUtil.HEADER_CONTENT_LENGTH, "0");
  }

  @Test
  void shouldDisableCertificateValidation() throws IOException {
    AdvancedHttpRequest request = new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    );

    request.disableCertificateValidation(true).request();

    verify(connection).setSSLSocketFactory(any(SSLSocketFactory.class));
  }

  @Test
  void shouldDisableHostnameValidation() throws IOException {
    AdvancedHttpRequest request = new AdvancedHttpRequest(
      client, HttpMethod.GET,"https://www.scm-manager.org"
    );

    request.disableHostnameValidation(true).request();

    verify(connection).setHostnameVerifier(any(HostnameVerifier.class));
  }

  @Test
  void shouldIgnoreProxySettings() throws IOException {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setEnableProxy(true);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).ignoreProxySettings(true).request();

    assertThat(proxy).isNull();
  }

  @Test
  void shouldUseProxyConnection() throws IOException {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setEnableProxy(true);

    new AdvancedHttpRequest(
      client, HttpMethod.GET,"https://www.scm-manager.org"
    ).request();

    assertThat(proxy).isNotNull();
  }

  @Test
  void shouldCreateTracingSpan() throws IOException {
    when(connection.getResponseCode()).thenReturn(200);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).spanKind("spaceships").request();

    verify(tracer).span("spaceships");
    verify(span).label("url", "https://www.scm-manager.org");
    verify(span).label("method", "GET");
    verify(span).label("status", 200);
    verify(span, never()).failed();
    verify(span).close();
  }

  @Test
  void shouldCreateFailedTracingSpan() throws IOException {
    when(connection.getResponseCode()).thenReturn(500);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).request();

    verify(tracer).span("HTTP Request");
    verify(span).label("url", "https://www.scm-manager.org");
    verify(span).label("method", "GET");
    verify(span).label("status", 500);
    verify(span).failed();
    verify(span).close();
  }

  @Test
  void shouldCreateFailedTracingSpanOnIOException() throws IOException {
    when(connection.getResponseCode()).thenThrow(new IOException("failed"));

    boolean thrown = false;
    try {

      new AdvancedHttpRequest(
        client, HttpMethod.DELETE, "http://failing.host"
      ).spanKind("failures").request();

    } catch (IOException ex) {
      thrown = true;
    }
    assertThat(thrown).isTrue();

    verify(tracer).span("failures");
    verify(span).label("url", "http://failing.host");
    verify(span).label("method", "DELETE");
    verify(span).label("exception", IOException.class.getName());
    verify(span).label("message", "failed");
    verify(span).failed();
    verify(span).close();
  }

  @Test
  void shouldNotCreateSpan() throws IOException {
    when(connection.getResponseCode()).thenReturn(200);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).disableTracing().request();

    verify(tracer, never()).span(anyString());
  }

  @Test
  void shouldNotTraceRequestIfAcceptedResponseCode() throws IOException {
    when(connection.getResponseCode()).thenReturn(400);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).acceptStatusCodes(400).request();

    verify(tracer).span("HTTP Request");
    verify(span).label("status", 400);
    verify(span, never()).failed();
    verify(span).close();
  }

  @Test
  void shouldTraceRequestAsFailedIfAcceptedResponseCodeDoesntMatch() throws IOException {
    when(connection.getResponseCode()).thenReturn(401);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).acceptStatusCodes(400).request();

    verify(tracer).span("HTTP Request");
    verify(span).label("status", 401);
    verify(span).failed();
    verify(span).close();
  }
}
