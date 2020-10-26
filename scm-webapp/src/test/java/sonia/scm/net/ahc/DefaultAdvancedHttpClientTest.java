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

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.TrustAllHostnameVerifier;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.util.HttpUtil;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.SocketAddress;
import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import sonia.scm.net.SSLContextProvider;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAdvancedHttpClientTest
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testApplyBaseSettings() throws IOException
  {
    new AdvancedHttpRequest(client, HttpMethod.GET,
      "https://www.scm-manager.org").request();
    verify(connection).setRequestMethod(HttpMethod.GET);
    verify(connection).setReadTimeout(DefaultAdvancedHttpClient.TIMEOUT_RAED);
    verify(connection).setConnectTimeout(
      DefaultAdvancedHttpClient.TIMEOUT_CONNECTION);
    verify(connection).addRequestProperty(HttpUtil.HEADER_CONTENT_LENGTH, "0");
  }

  @Test(expected = ContentTransformerNotFoundException.class)
  public void testContentTransformerNotFound(){
    client.createTransformer(String.class, "text/plain");
  }

  @Test
  public void testContentTransformer(){
    ContentTransformer transformer = mock(ContentTransformer.class);
    when(transformer.isResponsible(String.class, "text/plain")).thenReturn(Boolean.TRUE);
    transformers.add(transformer);
    ContentTransformer t = client.createTransformer(String.class, "text/plain");
    assertSame(transformer, t);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testApplyContent() throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    when(connection.getOutputStream()).thenReturn(baos);

    AdvancedHttpRequestWithBody request =
      new AdvancedHttpRequestWithBody(client, HttpMethod.PUT,
        "https://www.scm-manager.org");

    request.stringContent("test").request();
    verify(connection).setDoOutput(true);
    verify(connection).addRequestProperty(HttpUtil.HEADER_CONTENT_LENGTH, "4");
    assertEquals("test", baos.toString("UTF-8"));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testApplyHeaders() throws IOException
  {
    AdvancedHttpRequest request = new AdvancedHttpRequest(client,
                                    HttpMethod.POST,
                                    "http://www.scm-manager.org");

    request.header("Header-One", "One").header("Header-Two", "Two").request();
    verify(connection).setRequestMethod(HttpMethod.POST);
    verify(connection).addRequestProperty("Header-One", "One");
    verify(connection).addRequestProperty("Header-Two", "Two");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testApplyMultipleHeaders() throws IOException
  {
    AdvancedHttpRequest request = new AdvancedHttpRequest(client,
                                    HttpMethod.POST,
                                    "http://www.scm-manager.org");

    request.header("Header-One", "One").header("Header-One", "Two").request();
    verify(connection).setRequestMethod(HttpMethod.POST);
    verify(connection).addRequestProperty("Header-One", "One");
    verify(connection).addRequestProperty("Header-One", "Two");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testBodyRequestWithoutContent() throws IOException
  {
    AdvancedHttpRequestWithBody request =
      new AdvancedHttpRequestWithBody(client, HttpMethod.PUT,
        "https://www.scm-manager.org");

    request.request();
    verify(connection).addRequestProperty(HttpUtil.HEADER_CONTENT_LENGTH, "0");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testDisableCertificateValidation() throws IOException
  {
    AdvancedHttpRequest request = new AdvancedHttpRequest(client,
                                    HttpMethod.GET,
                                    "https://www.scm-manager.org");

    request.disableCertificateValidation(true).request();
    verify(connection).setSSLSocketFactory(any(SSLSocketFactory.class));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testDisableHostnameValidation() throws IOException
  {
    AdvancedHttpRequest request = new AdvancedHttpRequest(client,
                                    HttpMethod.GET,
                                    "https://www.scm-manager.org");

    request.disableHostnameValidation(true).request();
    verify(connection).setHostnameVerifier(any(TrustAllHostnameVerifier.class));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testIgnoreProxy() throws IOException
  {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setEnableProxy(true);
    new AdvancedHttpRequest(client, HttpMethod.GET,
      "https://www.scm-manager.org").ignoreProxySettings(true).request();
    assertFalse(client.proxyConnection);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testProxyConnection() throws IOException
  {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setEnableProxy(true);
    new AdvancedHttpRequest(client, HttpMethod.GET,
      "https://www.scm-manager.org").request();
    assertTrue(client.proxyConnection);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testProxyWithAuthentication() throws IOException
  {
    configuration.setProxyServer("proxy.scm-manager.org");
    configuration.setProxyPort(8090);
    configuration.setProxyUser("tricia");
    configuration.setProxyPassword("tricias secret");
    configuration.setEnableProxy(true);
    new AdvancedHttpRequest(client, HttpMethod.GET,
      "https://www.scm-manager.org").request();
    assertTrue(client.proxyConnection);
    verify(connection).addRequestProperty(
      DefaultAdvancedHttpClient.HEADER_PROXY_AUTHORIZATION,
      "Basic dHJpY2lhOnRyaWNpYXMgc2VjcmV0");
  }

  @Test
  public void shouldCreateTracingSpan() throws IOException {
    when(connection.getResponseCode()).thenReturn(200);

    new AdvancedHttpRequest(client, HttpMethod.GET, "https://www.scm-manager.org").spanKind("spaceships").request();
    verify(tracer).span("spaceships");
    verify(span).label("url", "https://www.scm-manager.org");
    verify(span).label("method", "GET");
    verify(span).label("status", 200);
    verify(span, never()).failed();
    verify(span).close();
  }

  @Test
  public void shouldCreateFailedTracingSpan() throws IOException {
    when(connection.getResponseCode()).thenReturn(500);

    new AdvancedHttpRequest(client, HttpMethod.GET, "https://www.scm-manager.org").request();
    verify(tracer).span("http-request");
    verify(span).label("url", "https://www.scm-manager.org");
    verify(span).label("method", "GET");
    verify(span).label("status", 500);
    verify(span).failed();
    verify(span).close();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    configuration = new ScmConfiguration();
    transformers = new HashSet<ContentTransformer>();
    client = new TestingAdvacedHttpClient(configuration, transformers);
    when(tracer.span(anyString())).thenReturn(span);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 15/05/01
   * @author         Enter your name here...
   */
  public class TestingAdvacedHttpClient extends DefaultAdvancedHttpClient
  {

    /**
     * Constructs ...
     *
     *
     * @param configuration
     * @param transformers
     */
    public TestingAdvacedHttpClient(ScmConfiguration configuration, Set<ContentTransformer> transformers)
    {
      super(configuration, tracer, transformers, new SSLContextProvider());
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param url
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException
    {
      return connection;
    }

    /**
     * Method description
     *
     *
     * @param url
     * @param address
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    protected HttpURLConnection createProxyConnecton(URL url,
      SocketAddress address)
      throws IOException
    {
      proxyConnection = true;

      return connection;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private boolean proxyConnection = false;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private TestingAdvacedHttpClient client;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  @Mock
  private HttpsURLConnection connection;

  /** Field description */
  private Set<ContentTransformer> transformers;

  @Mock
  private Tracer tracer;

  @Mock
  private Span span;
}
