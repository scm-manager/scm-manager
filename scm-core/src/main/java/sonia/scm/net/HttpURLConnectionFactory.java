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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * The {@link HttpURLConnectionFactory} simplifies the correct configuration of {@link HttpURLConnection}.
 * It sets timeout, proxy, ssl and authentication configurations to provide better defaults and respect SCM-Manager
 * settings.
 * <b>Note:</b> This class should only be used if a third party library requires an {@link HttpURLConnection}.
 * In all other cases the {@link sonia.scm.net.ahc.AdvancedHttpClient} should be used.
 */
public final class HttpURLConnectionFactory {

  private static final Logger LOG = LoggerFactory.getLogger(HttpURLConnectionFactory.class);

  static {
    // Allow basic authentication for proxies
    // https://stackoverflow.com/a/1626616
    System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

    // Set the default authenticator to our thread local authenticator
    Authenticator.setDefault(new ThreadLocalAuthenticator());
  }

  private final GlobalProxyConfiguration globalProxyConfiguration;
  private final Provider<TrustManager> trustManagerProvider;
  private final Connector connector;
  private final SSLContextFactory sslContextFactory;

  @Inject
  public HttpURLConnectionFactory(GlobalProxyConfiguration globalProxyConfiguration, Provider<TrustManager> trustManagerProvider) {
    this(globalProxyConfiguration, trustManagerProvider, new DefaultConnector(), new DefaultSSLContextFactory());
  }

  @VisibleForTesting
  public HttpURLConnectionFactory(GlobalProxyConfiguration globalProxyConfiguration, Provider<TrustManager> trustManagerProvider, Connector connector, SSLContextFactory sslContextFactory) {
    this.globalProxyConfiguration = globalProxyConfiguration;
    this.trustManagerProvider = trustManagerProvider;
    this.connector = connector;
    this.sslContextFactory = sslContextFactory;
  }

  /**
   * Creates a new {@link HttpURLConnection} from the given url with default options.
   * @param url url
   * @return a new connection with default options.
   * @throws IOException
   */
  public HttpURLConnection create(URL url) throws IOException {
    return create(url, new HttpConnectionOptions());
  }

  /**
   * Creates a new {@link HttpURLConnection} from the given url and options.
   * @param url url
   * @param options options for the new connection
   * @return a new connection with the given options
   * @throws IOException
   */
  public HttpURLConnection create(URL url, HttpConnectionOptions options) throws IOException {
    Preconditions.checkArgument(options != null, "Options are required");
    return new InternalConnectionFactory(options).create(url);
  }

  private class InternalConnectionFactory {

    private final HttpConnectionOptions options;

    private InternalConnectionFactory(HttpConnectionOptions options) {
      this.options = options;
    }

    HttpURLConnection create(URL url) throws IOException {
      // clear authentication this is required,
      // because we are not able to remove the authentication from thread local
      ThreadLocalAuthenticator.clear();

      ProxyConfiguration proxyConfiguration = options.getProxyConfiguration().filter(ProxyConfiguration::isEnabled).orElse(globalProxyConfiguration);
      if (isProxyEnabled(proxyConfiguration, url)) {
        return openProxyConnection(proxyConfiguration, url);
      }
      return configure(connector.connect(url, null));
    }

    private boolean isProxyEnabled(ProxyConfiguration proxyConfiguration, URL url) {
      return !options.isIgnoreProxySettings()
        && proxyConfiguration.isEnabled()
        && !isHostExcluded(proxyConfiguration, url);
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
        // Set the authentication for the proxy server for the current thread.
        // This becomes obsolete with java 9,
        // because the HttpURLConnection of java 9 has a setAuthenticator method
        // which makes it possible to set a proxy authentication for a single request.
        ThreadLocalAuthenticator.set(configuration);
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
      options.getConnectionProperties()
        .forEach(connection::setRequestProperty);
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

    private void applyBaseSettings(HttpURLConnection connection) {
      connection.setReadTimeout(options.getReadTimeout());
      connection.setConnectTimeout(options.getConnectionTimeout());
    }

  }

  @Value
  @VisibleForTesting
  static class ProxyAuthentication {
    String server;
    String username;
    char[] password;
  }

  @VisibleForTesting
  static class ThreadLocalAuthenticator extends Authenticator {

    private static final ThreadLocal<ProxyAuthentication> AUTHENTICATION = new ThreadLocal<>();

    static void set(ProxyConfiguration proxyConfiguration) {
      LOG.trace("configure proxy authentication for this thread");
      AUTHENTICATION.set(create(proxyConfiguration));
    }

    static void clear() {
      LOG.trace("release proxy authentication");
      AUTHENTICATION.remove();
    }

    @Nullable
    static ProxyAuthentication get() {
      return AUTHENTICATION.get();
    }

    @Nonnull
    private static ProxyAuthentication create(ProxyConfiguration proxyConfiguration) {
      return new ProxyAuthentication(
        proxyConfiguration.getHost(),
        proxyConfiguration.getUsername(),
        Strings.nullToEmpty(proxyConfiguration.getPassword()).toCharArray()
      );
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      if (getRequestorType() == RequestorType.PROXY) {
        ProxyAuthentication authentication = get();
        if (authentication != null && authentication.getServer().equals(getRequestingHost())) {
          LOG.debug("use proxy authentication for host {}", authentication.getServer());
          return new PasswordAuthentication(authentication.getUsername(), authentication.getPassword());
        }
      }
      return null;
    }
  }

  @VisibleForTesting
  @FunctionalInterface
  public interface Connector {

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
  public interface SSLContextFactory {

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
