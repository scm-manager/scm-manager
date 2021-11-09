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

package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * PushStateDispatcher which delegates the request to a different server. This dispatcher should only be used for
 * development and never in production.
 *
 * @since 2.0.0
 */
public final class ProxyPushStateDispatcher implements PushStateDispatcher {

  private static final Logger LOG = LoggerFactory.getLogger(ProxyPushStateDispatcher.class);

  @FunctionalInterface
  interface ConnectionFactory {

    HttpURLConnection open(URL url) throws IOException;

  }

  private final String target;
  private final ConnectionFactory connectionFactory;

  /**
   * Creates a new dispatcher for the given target. The target must be a valid url.
   *
   * @param target proxy target
   */
  public ProxyPushStateDispatcher(String target) {
    this(target, ProxyPushStateDispatcher::openConnection);
  }

  /**
   * This Constructor should only be used for testing.
   *
   * @param target proxy target
   * @param connectionFactory factory for creating an connection from a url
   */
  @VisibleForTesting
  ProxyPushStateDispatcher(String target, ConnectionFactory connectionFactory) {
    this.target = target;
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void dispatch(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
    URL url = createProxyUrl(uri);

    HttpURLConnection connection = connectionFactory.open(url);
    connection.setRequestMethod(request.getMethod());

    copyRequestHeaders(request, connection);
    if (request.getContentLength() > 0) {
      copyRequestBody(request, connection);
    }

    int responseCode = connection.getResponseCode();
    response.setStatus(responseCode);

    copyResponseHeaders(response, connection);
    int contentLength = connection.getContentLength();
    if (contentLength > 0 || contentLength == -1) {
      copyResponseBody(response, connection);
    }
  }

  private void copyResponseBody(HttpServletResponse response, HttpURLConnection connection) throws IOException {
    try (InputStream input = getConnectionInput(connection); OutputStream output = response.getOutputStream()) {
      ByteStreams.copy(input, output);
    }
  }

  private InputStream getConnectionInput(HttpURLConnection connection) throws IOException {
    if (connection.getErrorStream() != null) {
      return connection.getErrorStream();
    }
    return connection.getInputStream();
  }

  private void copyResponseHeaders(HttpServletResponse response, HttpURLConnection connection) {
    Map<String, List<String>> headerFields = connection.getHeaderFields();
    for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
      if (entry.getKey() != null && !"Transfer-Encoding".equalsIgnoreCase(entry.getKey())) {
        for (String value : entry.getValue()) {
          response.addHeader(entry.getKey(), value);
        }
      }
    }
  }

  private void copyRequestBody(HttpServletRequest request, HttpURLConnection connection) throws IOException {
    connection.setDoOutput(true);
    try (InputStream input = request.getInputStream(); OutputStream output = connection.getOutputStream()) {
      ByteStreams.copy(input, output);
    }
  }

  private void copyRequestHeaders(HttpServletRequest request, HttpURLConnection connection) {
    Enumeration<String> headers = request.getHeaderNames();
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      Enumeration<String> values = request.getHeaders(header);
      while (values.hasMoreElements()) {
        String value = values.nextElement();
        connection.setRequestProperty(header, value);
      }
    }
  }

  private URL createProxyUrl(String uri) throws MalformedURLException {
    return new URL(target + uri);
  }

  private static HttpURLConnection openConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
  }
}
