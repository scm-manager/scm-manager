package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
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
    try {
      proxy(request, response, uri);
    } catch (FileNotFoundException ex) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void proxy(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
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

    appendProxyHeader(response, url);

    copyResponseBody(response, connection);
  }

  private void appendProxyHeader(HttpServletResponse response, URL url) {
    response.addHeader("X-Forwarded-Port", String.valueOf(url.getPort()));
  }

  private void copyResponseBody(HttpServletResponse response, HttpURLConnection connection) throws IOException {
    try (InputStream input = connection.getInputStream(); OutputStream output = response.getOutputStream()) {
      ByteStreams.copy(input, output);
    }
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
