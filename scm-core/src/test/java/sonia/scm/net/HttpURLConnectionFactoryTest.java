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

package sonia.scm.net;

import com.google.common.collect.ImmutableSet;
import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.HttpURLConnectionFactory.DefaultSSLContextFactory;
import sonia.scm.net.HttpURLConnectionFactory.ProxyAuthentication;
import sonia.scm.net.HttpURLConnectionFactory.ThreadLocalAuthenticator;
import sonia.scm.net.HttpURLConnectionFactory.TrustAllHostnameVerifier;
import sonia.scm.net.HttpURLConnectionFactory.TrustAllTrustManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HttpURLConnectionFactoryTest {

  @Test
  void shouldFailWithNonHttpURL() throws MalformedURLException {
    URLConnection urlConnection = mock(URLConnection.class);
    HttpURLConnectionFactory factory = new HttpURLConnectionFactory(
      new GlobalProxyConfiguration(new ScmConfiguration()),
      () -> null,
      (url, proxy) -> urlConnection,
      new DefaultSSLContextFactory()
    );

    URL url = new URL("ftp://ftp.hitchhiker.com");
    assertThrows(IllegalArgumentException.class, () -> factory.create(url));
  }

  @Test
  void shouldFailWithInvalidStateException() throws IOException {
    HttpURLConnectionFactory factory = new HttpURLConnectionFactory(
      new GlobalProxyConfiguration(new ScmConfiguration()),
      () -> mock(TrustManager.class),
      (url, proxy) -> mock(HttpsURLConnection.class),
      () -> SSLContext.getInstance("TheAlgoThatDoesNotExists")
    );

    URL url = new URL("https://hitchhiker.com");
    assertThrows(IllegalStateException.class, () -> factory.create(url));
  }

  @Test
  void shouldCreateHttpConnection() throws IOException {
    URLConnection urlConnection = mock(HttpURLConnection.class);
    HttpURLConnectionFactory factory = new HttpURLConnectionFactory(
      new GlobalProxyConfiguration(new ScmConfiguration()),
      () -> null,
      (url, proxy) -> urlConnection,
      new DefaultSSLContextFactory()
    );

    HttpURLConnection connection = factory.create(new URL("http://hitchhiker.com"));
    assertThat(connection).isNotNull();
  }

  @Test
  void shouldThrowWithNonExistentConnectionOptions() throws MalformedURLException {
    URLConnection urlConnection = mock(HttpURLConnection.class);
    HttpURLConnectionFactory factory = new HttpURLConnectionFactory(
      new GlobalProxyConfiguration(new ScmConfiguration()),
      () -> null,
      (url, proxy) -> urlConnection,
      new DefaultSSLContextFactory()
    );
    final URL url = new URL("http://hitchhiker.com");

    assertThrows(IllegalArgumentException.class, () -> factory.create(url, null));
  }

  @Nested
  class HttpsConnectionTests {

    private ScmConfiguration configuration;

    @Mock
    private TrustManager trustManager;

    private SSLContext sslContext;

    private HttpURLConnectionFactory connectionFactory;

    private Proxy usedProxy;

    @BeforeEach
    void setUpConnectionFactory() throws NoSuchAlgorithmException {
      this.configuration = new ScmConfiguration();
      this.sslContext = spy(new DefaultSSLContextFactory().create());

      this.connectionFactory = new HttpURLConnectionFactory(
        new GlobalProxyConfiguration(configuration),
        Providers.of(trustManager),
        (url, proxy) -> {
          this.usedProxy = proxy;
          return mock(HttpsURLConnection.class);
        },
        () -> sslContext
      );
    }

    @Test
    void shouldCreateDefaultHttpConnection() throws IOException {
      HttpURLConnection connection = connectionFactory.create(new URL("https://hitchhiker.org"));

      verify(connection).setConnectTimeout(HttpConnectionOptions.DEFAULT_CONNECTION_TIMEOUT);
      verify(connection).setReadTimeout(HttpConnectionOptions.DEFAULT_READ_TIMEOUT);
      assertThat(usedProxy).isNull();
    }

    @Test
    void shouldUseProvidedConnectionTimeout() throws IOException {
      HttpURLConnection connection = connectionFactory.create(
        new URL("https://hitchhiker.org"),
        new HttpConnectionOptions().withConnectionTimeout(5L, TimeUnit.SECONDS)
      );
      verify(connection).setConnectTimeout(5000);
    }

    @Test
    void shouldUseProvidedReadTimeout() throws IOException {
      HttpURLConnection connection = connectionFactory.create(
        new URL("https://hitchhiker.org"),
        new HttpConnectionOptions().withReadTimeout(3L, TimeUnit.SECONDS)
      );
      verify(connection).setReadTimeout(3000);
    }

    @Test
    void shouldUseGlobalProxyConfigurationIfLocalOneIsDisabled() throws IOException {
      configuration.setProxyServer("proxy.hitchhiker.com");
      configuration.setProxyPort(3128);
      configuration.setEnableProxy(true);

      ScmConfiguration localProxyConf = new ScmConfiguration();
      localProxyConf.setEnableProxy(false);
      localProxyConf.setProxyServer("prox.hitchhiker.net");
      localProxyConf.setProxyPort(3127);

      connectionFactory.create(new URL("https://hitchhiker.org"), new HttpConnectionOptions().withProxyConfiguration(new GlobalProxyConfiguration(localProxyConf)));

      assertUsedProxy("proxy.hitchhiker.com", 3128);
    }

    @Test
    void shouldCreateProxyConnection() throws IOException {
      configuration.setEnableProxy(true);
      configuration.setProxyServer("proxy.hitchhiker.com");
      configuration.setProxyPort(3128);

      connectionFactory.create(new URL("https://hitchhiker.org"));

      assertUsedProxy("proxy.hitchhiker.com", 3128);
    }

    @Test
    void shouldNotCreateProxyConnectionIfHostIsOnTheExcludeList() throws IOException {
      configuration.setEnableProxy(true);
      configuration.setProxyServer("proxy.hitchhiker.com");
      configuration.setProxyPort(3128);
      configuration.setProxyExcludes(ImmutableSet.of("localhost", "hitchhiker.org", "127.0.0.1"));

      connectionFactory.create(new URL("https://hitchhiker.org"));

      assertThat(usedProxy).isNull();
    }

    @Test
    void shouldNotCreateProxyConnectionWithIgnoreOption() throws IOException {
      configuration.setEnableProxy(true);
      configuration.setProxyServer("proxy.hitchhiker.com");
      configuration.setProxyPort(3128);

      connectionFactory.create(
        new URL("https://hitchhiker.org"), new HttpConnectionOptions().withIgnoreProxySettings()
      );

      assertThat(usedProxy).isNull();
    }

    @Test
    void shouldCreateProxyConnectionWithAuthentication() throws IOException {
      configuration.setEnableProxy(true);
      configuration.setProxyServer("proxy.hitchhiker.org");
      configuration.setProxyPort(3129);
      configuration.setProxyUser("marvin");
      configuration.setProxyPassword("brainLikeAPlanet");

      connectionFactory.create(new URL("https://hitchhiker.org"));

      assertUsedProxy("proxy.hitchhiker.org", 3129);

      assertProxyAuthentication("marvin", "brainLikeAPlanet");
    }

    private void assertProxyAuthentication(String username, String password) {
      ProxyAuthentication proxyAuthentication = ThreadLocalAuthenticator.get();
      assertThat(proxyAuthentication).isNotNull();
      assertThat(proxyAuthentication.getUsername()).isEqualTo(username);
      assertThat(proxyAuthentication.getPassword()).isEqualTo(password.toCharArray());
    }

    @Test
    void shouldCreateProxyConnectionFromOptions() throws IOException {
      ScmConfiguration localProxyConf = new ScmConfiguration();
      localProxyConf.setEnableProxy(true);
      localProxyConf.setProxyServer("prox.hitchhiker.net");
      localProxyConf.setProxyPort(3127);
      localProxyConf.setProxyUser("trillian");
      localProxyConf.setProxyPassword("secret");

      connectionFactory.create(
        new URL("https://hitchhiker.net"),
        new HttpConnectionOptions().withProxyConfiguration(new GlobalProxyConfiguration(localProxyConf))
      );

      assertUsedProxy("prox.hitchhiker.net", 3127);
      assertProxyAuthentication("trillian", "secret");
    }

    @Test
    void shouldNotUsePreviousProxyAuthentication() throws IOException {
      ScmConfiguration localProxyConf = new ScmConfiguration();
      localProxyConf.setEnableProxy(true);
      localProxyConf.setProxyServer("proxy.hitchhiker.net");
      localProxyConf.setProxyPort(3127);
      localProxyConf.setProxyUser("trillian");
      localProxyConf.setProxyPassword("secret");

      URL url = new URL("https://hitchhiker.net");
      HttpConnectionOptions options = new HttpConnectionOptions()
        .withProxyConfiguration(new GlobalProxyConfiguration(localProxyConf));

      connectionFactory.create(url, options);
      assertUsedProxy("proxy.hitchhiker.net", 3127);
      assertProxyAuthentication("trillian", "secret");

      localProxyConf.setEnableProxy(false);
      connectionFactory.create(url, options);
      assertThat(usedProxy).isNull();
      assertThat(ThreadLocalAuthenticator.get()).isNull();
    }

    @Test
    void shouldUseProvidedTrustManagerForHttpsConnections() throws IOException, KeyManagementException {
      HttpURLConnection connection = connectionFactory.create(new URL("https://hitchhiker.net"));

      TrustManager[] trustManagers = usedTrustManagers(connection);
      assertThat(trustManagers).containsOnly(trustManager);
    }

    @Test
    void shouldUseTrustAllTrustManager() throws IOException, KeyManagementException {
      HttpURLConnection connection = connectionFactory.create(
        new URL("https://hitchhiker.net"),
        new HttpConnectionOptions().withDisableCertificateValidation()
      );

      TrustManager[] trustManagers = usedTrustManagers(connection);
      assertThat(trustManagers).hasSize(1).hasOnlyElementsOfType(TrustAllTrustManager.class);
    }

    @Test
    void shouldUseTrustAllHostnameVerifier() throws IOException {
      HttpURLConnection connection = connectionFactory.create(
        new URL("https://hitchhiker.net"),
        new HttpConnectionOptions().withDisabledHostnameValidation()
      );

      assertThat(connection).isInstanceOfSatisfying(
        HttpsURLConnection.class, https -> {
          ArgumentCaptor<HostnameVerifier> captor = ArgumentCaptor.forClass(HostnameVerifier.class);
          verify(https).setHostnameVerifier(captor.capture());
          assertThat(captor.getValue()).isInstanceOf(TrustAllHostnameVerifier.class);
        }
      );
    }

    @Test
    void shouldUseProvidedKeyManagers() throws IOException, KeyManagementException {
      KeyManager keyManager = mock(KeyManager.class);
      connectionFactory.create(
        new URL("https://hitchhiker.net"),
        new HttpConnectionOptions().withKeyManagers(keyManager)
      );

      ArgumentCaptor<KeyManager[]> captor = ArgumentCaptor.forClass(KeyManager[].class);
      verify(sslContext).init(captor.capture(), any(), eq(null));
      KeyManager[] keyManagers = captor.getValue();

      assertThat(keyManagers).containsOnly(keyManager);
    }

    @Test
    void shouldSetGivenRequestProperties() throws IOException {
      HttpConnectionOptions options = new HttpConnectionOptions();
      options.addRequestProperty("valid_until", "end of the universe");

      HttpURLConnection connection = connectionFactory.create(new URL("https://hitchhiker.org"), options);

      verify(connection).setRequestProperty("valid_until", "end of the universe");
      assertThat(usedProxy).isNull();
    }

    private TrustManager[] usedTrustManagers(HttpURLConnection connection) throws KeyManagementException {
      ArgumentCaptor<TrustManager[]> captor = ArgumentCaptor.forClass(TrustManager[].class);
      assertThat(connection).isInstanceOfSatisfying(
        HttpsURLConnection.class, https -> verify(https).setSSLSocketFactory(any())
      );

      verify(sslContext).init(eq(null), captor.capture(), eq(null));
      verify(sslContext).getSocketFactory();

      return captor.getValue();
    }

    private void assertUsedProxy(String host, int port) {
      assertThat(usedProxy).isNotNull();
      assertThat(usedProxy.address()).isInstanceOfSatisfying(InetSocketAddress.class, inet -> {
        assertThat(inet.getHostName()).isEqualTo(host);
        assertThat(inet.getPort()).isEqualTo(port);
      });
    }


  }

}
