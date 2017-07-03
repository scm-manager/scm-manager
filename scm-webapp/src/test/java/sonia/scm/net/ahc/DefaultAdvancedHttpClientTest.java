/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.TrustAllHostnameVerifier;
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    configuration = new ScmConfiguration();
    transformers = new HashSet<>();
    client = new TestingAdvacedHttpClient(configuration, transformers);
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
    public TestingAdvacedHttpClient(ScmConfiguration configuration,
      Set<ContentTransformer> transformers)
    {
      super(configuration, transformers, new SSLContextProvider());
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
}
