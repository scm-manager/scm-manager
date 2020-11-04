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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import org.apache.shiro.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.Proxies;
import sonia.scm.net.TrustAllHostnameVerifier;
import sonia.scm.net.TrustAllTrustManager;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.util.HttpUtil;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * Default implementation of the {@link AdvancedHttpClient}. The default
 * implementation uses {@link HttpURLConnection}.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
public class DefaultAdvancedHttpClient extends AdvancedHttpClient
{

  /** proxy authorization header */
  @VisibleForTesting
  static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

  /** connection timeout */
  @VisibleForTesting
  static final int TIMEOUT_CONNECTION = 30000;

  /** read timeout */
  @VisibleForTesting
  static final int TIMEOUT_RAED = 1200000;

  /** credential separator */
  private static final String CREDENTIAL_SEPARATOR = ":";

  /** basic authentication prefix */
  private static final String PREFIX_BASIC_AUTHENTICATION = "Basic ";

  /**
   * the logger for DefaultAdvancedHttpClient
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAdvancedHttpClient.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link DefaultAdvancedHttpClient}.
   *
   *
   * @param configuration scm-manager main configuration
   * @param contentTransformers content transformer
   * @param sslContextProvider ssl context provider
   */
  @Inject
  public DefaultAdvancedHttpClient(ScmConfiguration configuration,
    Tracer tracer, Set<ContentTransformer> contentTransformers, Provider<SSLContext> sslContextProvider)
  {
    this.configuration = configuration;
    this.tracer = tracer;
    this.contentTransformers = contentTransformers;
    this.sslContextProvider = sslContextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new {@link HttpURLConnection} from the given {@link URL}. The
   * method is visible for testing.
   *
   *
   * @param url url
   *
   * @return new {@link HttpURLConnection}
   *
   * @throws IOException
   */
  @VisibleForTesting
  protected HttpURLConnection createConnection(URL url) throws IOException
  {
    return (HttpURLConnection) url.openConnection();
  }

  /**
   * Creates a new proxy {@link HttpURLConnection} from the given {@link URL}
   * and {@link SocketAddress}. The method is visible for testing.
   *
   *
   * @param url url
   * @param address proxy socket address
   *
   * @return new proxy {@link HttpURLConnection}
   *
   * @throws IOException
   */
  @VisibleForTesting
  protected HttpURLConnection createProxyConnecton(URL url,
    SocketAddress address)
    throws IOException
  {
    return (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP,
      address));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ContentTransformer createTransformer(Class<?> type, String contentType)
  {
    ContentTransformer responsible = null;

    for (ContentTransformer transformer : contentTransformers)
    {
      if (transformer.isResponsible(type, contentType))
      {
        responsible = transformer;

        break;
      }
    }

    if (responsible == null)
    {
      throw new ContentTransformerNotFoundException(
        "could not find content transformer for content type ".concat(
          contentType));
    }

    return responsible;
  }

  /**
   * Executes the given request and returns the server response.
   *
   *
   * @param request http request
   *
   * @return server response
   *
   * @throws IOException
   */
  @Override
  protected AdvancedHttpResponse request(BaseHttpRequest<?> request) throws IOException {
    String spanKind = request.getSpanKind();
    if (Strings.isNullOrEmpty(spanKind)) {
      logger.debug("execute request {} without tracing", request.getUrl());
      return doRequest(request);
    }
    return doRequestWithTracing(request);
  }

  @Nonnull
  private DefaultAdvancedHttpResponse doRequestWithTracing(BaseHttpRequest<?> request) throws IOException {
    try (Span span = tracer.span(request.getSpanKind())) {
      span.label("url", request.getUrl());
      span.label("method", request.getMethod());
      try {
        DefaultAdvancedHttpResponse response = doRequest(request);
        span.label("status", response.getStatus());
        if (!response.isSuccessful()) {
          span.failed();
        }
        return response;
      } catch (IOException ex) {
        span.label("exception", ex.getClass().getName());
        span.label("message", ex.getMessage());
        span.failed();
        throw ex;
      }
    }
  }

  @Nonnull
  private DefaultAdvancedHttpResponse doRequest(BaseHttpRequest<?> request) throws IOException {
    HttpURLConnection connection = openConnection(request, new URL(request.getUrl()));

    applyBaseSettings(request, connection);

    if (connection instanceof HttpsURLConnection) {
      applySSLSettings(request, (HttpsURLConnection) connection);
    }

    Content content = null;

    if (request instanceof AdvancedHttpRequestWithBody) {
      AdvancedHttpRequestWithBody ahrwb = (AdvancedHttpRequestWithBody) request;

      content = ahrwb.getContent();

      if (content != null) {
        content.prepare(ahrwb);
      } else {
        request.header(HttpUtil.HEADER_CONTENT_LENGTH, "0");
      }
    } else {
      request.header(HttpUtil.HEADER_CONTENT_LENGTH, "0");
    }

    applyHeaders(request, connection);

    if (content != null) {
      applyContent(connection, content);
    }

    return new DefaultAdvancedHttpResponse(this, connection,
      connection.getResponseCode(), connection.getResponseMessage());
  }

  private void appendProxyAuthentication(HttpURLConnection connection)
  {
    String username = configuration.getProxyUser();
    String password = configuration.getProxyPassword();

    if (!Strings.isNullOrEmpty(username) ||!Strings.isNullOrEmpty(password))
    {
      logger.debug("append proxy authentication header for user {}", username);

      String auth = username.concat(CREDENTIAL_SEPARATOR).concat(password);

      auth = Base64.encodeToString(auth.getBytes());
      connection.addRequestProperty(HEADER_PROXY_AUTHORIZATION,
        PREFIX_BASIC_AUTHENTICATION.concat(auth));
    }
  }

  private void applyBaseSettings(BaseHttpRequest<?> request,
    HttpURLConnection connection)
    throws ProtocolException
  {
    connection.setRequestMethod(request.getMethod());
    connection.setReadTimeout(TIMEOUT_RAED);
    connection.setConnectTimeout(TIMEOUT_CONNECTION);
  }

  private void applyContent(HttpURLConnection connection, Content content)
    throws IOException
  {
    connection.setDoOutput(true);

    OutputStream output = null;

    try
    {
      output = connection.getOutputStream();
      content.process(output);
    }
    finally
    {
      Closeables.close(output, true);
    }
  }

  private void applyHeaders(BaseHttpRequest<?> request,
    HttpURLConnection connection)
  {
    Multimap<String, String> headers = request.getHeaders();

    for (String key : headers.keySet())
    {
      for (String value : headers.get(key))
      {
        connection.addRequestProperty(key, value);
      }
    }
  }

  private void applySSLSettings(BaseHttpRequest<?> request,
    HttpsURLConnection connection)
  {
    if (request.isDisableCertificateValidation())
    {
      logger.trace("disable certificate validation");

      try
      {
        TrustManager[] trustAllCerts = new TrustManager[] {
                                         new TrustAllTrustManager() };
        SSLContext sc = SSLContext.getInstance("TLS");

        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());
      }
      catch (KeyManagementException | NoSuchAlgorithmException ex)
      {
        logger.error("could not disable certificate validation", ex);
      }
    }
    else
    {
      logger.trace("set ssl socket factory from provider");
      connection.setSSLSocketFactory(sslContextProvider.get().getSocketFactory());
    }

    if (request.isDisableHostnameValidation())
    {
      logger.trace("disable hostname validation");
      connection.setHostnameVerifier(new TrustAllHostnameVerifier());
    }
  }

  private HttpURLConnection openConnection(BaseHttpRequest<?> request, URL url)
    throws IOException
  {
    HttpURLConnection connection;

    if (isProxyEnabled(request))
    {
      connection = openProxyConnection(url);
      appendProxyAuthentication(connection);
    }
    else
    {
      if (request.isIgnoreProxySettings())
      {
        logger.trace("ignore proxy settings");
      }

      if (logger.isDebugEnabled()) {
        logger.debug("fetch {}", url.toExternalForm());
      }

      connection = createConnection(url);
    }

    return connection;
  }

  private HttpURLConnection openProxyConnection(URL url)
    throws IOException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch '{}' using proxy {}:{}", url.toExternalForm(),
        configuration.getProxyServer(), configuration.getProxyPort());
    }

    SocketAddress address =
      new InetSocketAddress(configuration.getProxyServer(),
        configuration.getProxyPort());

    return createProxyConnecton(url, address);
  }

  //~--- get methods ----------------------------------------------------------

  private boolean isProxyEnabled(BaseHttpRequest<?> request)
  {
    return !request.isIgnoreProxySettings()
      && Proxies.isEnabled(configuration, request.getUrl());
  }

  //~--- fields ---------------------------------------------------------------

  /** scm-manager main configuration */
  private final ScmConfiguration configuration;

  /** set of content transformers */
  private final Set<ContentTransformer> contentTransformers;

  /** ssl context provider */
  private final Provider<SSLContext> sslContextProvider;

  /** tracer used for request tracing */
  private final Tracer tracer;
}
