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

package sonia.scm.net.ahc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.SSLContextProvider;
import sonia.scm.net.TrustAllHostnameVerifier;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.util.HttpUtil;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketAddress;
import java.net.URL;
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

/**
 *
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class DefaultAdvancedHttpClientTest {

  @Mock
  private HttpsURLConnection connection;

  @Mock
  private Tracer tracer;

  @Mock
  private Span span;

  private Set<ContentTransformer> transformers;

  private ScmConfiguration configuration;

  private TestingAdvacedHttpClient client;

  @BeforeEach
  void setUp() {
    configuration = new ScmConfiguration();
    transformers = new HashSet<>();
    client = new TestingAdvacedHttpClient(configuration, transformers);
    lenient().when(tracer.span(anyString())).thenReturn(span);
  }

  @Test
  void shouldApplyBaseSettings() throws IOException {
    new AdvancedHttpRequest(client, HttpMethod.GET,
      "https://www.scm-manager.org").request();
    verify(connection).setRequestMethod(HttpMethod.GET);
    verify(connection).setReadTimeout(DefaultAdvancedHttpClient.TIMEOUT_RAED);
    verify(connection).setConnectTimeout(
      DefaultAdvancedHttpClient.TIMEOUT_CONNECTION);
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

    verify(connection).setHostnameVerifier(any(TrustAllHostnameVerifier.class));
  }

  @Test
  void shouldIgnoreProxySettings() throws IOException {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setEnableProxy(true);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).ignoreProxySettings(true).request();

    assertThat(client.proxyConnection).isFalse();
  }

  @Test
  void shouldUseProxyConnection() throws IOException {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setEnableProxy(true);

    new AdvancedHttpRequest(
      client, HttpMethod.GET,"https://www.scm-manager.org"
    ).request();

    assertThat(client.proxyConnection).isTrue();
  }

  @Test
  void shouldUseProxyWithAuthentication() throws IOException {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setProxyUser("tricia");
    configuration.setProxyPassword("tricias secret");
    configuration.setEnableProxy(true);

    new AdvancedHttpRequest(
      client, HttpMethod.GET, "https://www.scm-manager.org"
    ).request();

    assertThat(client.proxyConnection).isTrue();
    verify(connection).addRequestProperty(
      DefaultAdvancedHttpClient.HEADER_PROXY_AUTHORIZATION, "Basic dHJpY2lhOnRyaWNpYXMgc2VjcmV0"
    );
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

  public class TestingAdvacedHttpClient extends DefaultAdvancedHttpClient {

    private boolean proxyConnection = false;


    public TestingAdvacedHttpClient(ScmConfiguration configuration, Set<ContentTransformer> transformers) {
      super(configuration, tracer, transformers, new SSLContextProvider());
    }

    @Override
    protected HttpURLConnection createConnection(URL url) {
      return connection;
    }
    @Override
    protected HttpURLConnection createProxyConnecton(URL url, SocketAddress address) {
      proxyConnection = true;
      return connection;
    }

  }
}
