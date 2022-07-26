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

package sonia.scm.web.tailwind;

import com.github.sdorra.webresources.CacheControl;
import com.github.sdorra.webresources.WebResourceSender;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.filter.WebElement;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.UberWebResourceLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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

import static sonia.scm.PushStateDispatcherProvider.PROPERTY_TARGET;

@Singleton
@WebElement(value = "/styles.bundle.css")
public class StylesServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(StylesServlet.class);
  private final UberWebResourceLoader webResourceLoader;
  private final String target = System.getProperty(PROPERTY_TARGET);

  private final WebResourceSender sender = WebResourceSender.create()
    .withGZIP()
    .withGZIPMinLength(512)
    .withBufferSize(16384)
    .withCacheControl(CacheControl.create().noCache());

  @Inject
  public StylesServlet(PluginLoader pluginLoader) {
    this.webResourceLoader = pluginLoader.getUberWebResourceLoader();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      URL url = webResourceLoader.getResource("/assets/webapp.bundle.css");
      if (url != null) {
        // TODO: Merge css
        sender.resource(url).get(request, response);
      } else {
        getLocally(createProxyUrl("/assets/webapp.tailwind.css"), request, response);
      }
    } catch (IOException ex) {
      LOG.error("Error on getting the tailwind stylesheet", ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void getLocally(URL url, HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpURLConnection connection = openConnection(url);
    connection.setRequestMethod(request.getMethod());
//    copyRequestHeaders(request, connection);

    int responseCode = connection.getResponseCode();
    response.setStatus(responseCode);
    try (InputStream input = getConnectionInput(connection); OutputStream output = response.getOutputStream()) {
      ByteStreams.copy(input, output);
    }
//    copyResponseHeaders(response, connection);
  }

  private InputStream getConnectionInput(HttpURLConnection connection) throws IOException {
    if (connection.getErrorStream() != null) {
      return connection.getErrorStream();
    }
    return connection.getInputStream();
  }

  private URL createProxyUrl(String uri) throws MalformedURLException {
    return new URL(target + uri);
  }

  private static HttpURLConnection openConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
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

}
