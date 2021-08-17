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

import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class HttpURLConnectionFactory {

  private static final Logger LOG = LoggerFactory.getLogger(HttpURLConnectionFactory.class);

  private static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";
  private static final String PREFIX_BASIC_AUTHENTICATION = "Basic ";
  private static final String CREDENTIAL_SEPARATOR = ":";

  private final GlobalProxyConfiguration globalProxyConfiguration;
  private final Provider<TrustManager> trustManagerProvider;
  private final Connector connector;
  private final SSLContextFactory sslContextFactory;

  @Inject
  public HttpURLConnectionFactory(GlobalProxyConfiguration globalProxyConfiguration, Provider<TrustManager> trustManagerProvider) {
    this(globalProxyConfiguration, trustManagerProvider, new DefaultConnector(), new DefaultSSLContextFactory());
  }

  @VisibleForTesting
  HttpURLConnectionFactory(GlobalProxyConfiguration globalProxyConfiguration, Provider<TrustManager> trustManagerProvider, Connector connector, SSLContextFactory sslContextFactory) {
    this.globalProxyConfiguration = globalProxyConfiguration;
    this.trustManagerProvider = trustManagerProvider;
    this.connector = connector;
    this.sslContextFactory = sslContextFactory;
  }

  public HttpURLConnection create(URL url) throws IOException {
    return create(url, new HttpConnectionOptions());
  }

  public HttpURLConnection create(URL url, HttpConnectionOptions options) throws IOException {
    return new InternalConnectionFactory(options).create(url);
  }

  private class InternalConnectionFactory {

    private final HttpConnectionOptions options;

    private InternalConnectionFactory(HttpConnectionOptions options) {
      this.options = options;
    }

    HttpURLConnection create(URL url) throws IOException {
      ProxyConfiguration proxyConfiguration = options.getProxyConfiguration().orElse(globalProxyConfiguration);
      if (isProxyEnabled(proxyConfiguration, url)) {
        return openProxyConnection(proxyConfiguration, url);
      }
      return configure(connector.connect(url, null));
    }

    private boolean isProxyEnabled(ProxyConfiguration proxyConfiguration, URL url) {
      return proxyConfiguration.isEnabled() && !isHostExcluded(proxyConfiguration, url);
    }

    private boolean isHostExcluded(ProxyConfiguration proxyConfiguration, URL url) {
      Collection<String> excludes = proxyConfiguration.getExcludes();
      if (excludes == null) {
        return false;
      }
      return excludes.contains(url.getHost());
    }

    private HttpURLConnection openProxyConnection(ProxyConfiguration configuration, URL url) throws IOException {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "open connection to '{}' using proxy {}:{}",
          url.toExternalForm(), configuration.getHost(), configuration.getPort()
        );
      }

      SocketAddress address = new InetSocketAddress(configuration.getHost(), configuration.getPort());

      HttpURLConnection connection = configure(connector.connect(url, new Proxy(Proxy.Type.HTTP, address)));
      if (configuration.isAuthenticationRequired()) {
        applyProxyAuthentication(configuration, connection);
      }

      return connection;
    }

    private HttpURLConnection configure(URLConnection urlConnection) {
      if (!(urlConnection instanceof HttpURLConnection)) {
        throw new IllegalArgumentException("only http(s) urls are supported");
      }
      HttpURLConnection connection = (HttpURLConnection) urlConnection;
      applyBaseSettings(connection);
      if (connection instanceof HttpsURLConnection) {
        applySSLSettings((HttpsURLConnection) connection);
      }
      return connection;
    }

    private void applySSLSettings(HttpsURLConnection connection) {
      connection.setSSLSocketFactory(createSSLContext().getSocketFactory());

      if (options.isDisableHostnameValidation()) {
        disableHostnameVerification(connection);
      }
    }

    private SSLContext createSSLContext() {
      return createSSLContext(createTrustManager(), options.getKeyManagers().orElse(null));
    }

    private TrustManager createTrustManager() {
      if (options.isDisableCertificateValidation()) {
        LOG.warn("certificate validation is disabled");
        return new TrustAllTrustManager();
      }
      return trustManagerProvider.get();
    }

    private SSLContext createSSLContext(TrustManager trustManager, KeyManager[] keyManagers) {
      try {
        SSLContext sc = sslContextFactory.create();
        sc.init(keyManagers, new TrustManager[]{trustManager}, null);
        return sc;
      } catch (KeyManagementException | NoSuchAlgorithmException ex) {
        throw new IllegalStateException("failed to configure ssl context", ex);
      }
    }

    private void disableHostnameVerification(HttpsURLConnection connection) {
      LOG.trace("disable hostname validation");
      connection.setHostnameVerifier(new TrustAllHostnameVerifier());
    }

    private void applyProxyAuthentication(ProxyConfiguration configuration, HttpURLConnection connection) {
      String username = configuration.getUsername();
      String password = configuration.getPassword();

      String auth = username.concat(CREDENTIAL_SEPARATOR).concat(password);
      connection.setRequestProperty(
        HEADER_PROXY_AUTHORIZATION,
        PREFIX_BASIC_AUTHENTICATION.concat(Base64.encodeToString(auth.getBytes()))
      );
    }

    private void applyBaseSettings(HttpURLConnection connection) {
      connection.setReadTimeout(options.getReadTimeout());
      connection.setConnectTimeout(options.getConnectionTimeout());
    }

  }

  @VisibleForTesting
  @FunctionalInterface
  interface Connector {

    URLConnection connect(URL url, @Nullable Proxy proxy) throws IOException;

  }

  private static class DefaultConnector implements Connector {

    @Override
    public URLConnection connect(URL url, @Nullable Proxy proxy) throws IOException {
      if (proxy != null) {
        return url.openConnection(proxy);
      }
      return url.openConnection();
    }

  }

  @VisibleForTesting
  @FunctionalInterface
  interface SSLContextFactory {

    SSLContext create() throws NoSuchAlgorithmException;

  }

  @VisibleForTesting
  static class DefaultSSLContextFactory implements SSLContextFactory {

    @Override
    public SSLContext create() throws NoSuchAlgorithmException {
      return SSLContext.getInstance("TLS");
    }
  }

  @SuppressWarnings("java:S4830")
  @VisibleForTesting
  static class TrustAllTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
      // accept everything
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
      // accept everything
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  @SuppressWarnings("java:S5527")
  @VisibleForTesting
  static class TrustAllHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  }

}
