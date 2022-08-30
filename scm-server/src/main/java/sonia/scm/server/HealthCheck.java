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

package sonia.scm.server;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class HealthCheck implements Callable<Integer> {

  private final List<Listener> listeners;

  public HealthCheck(List<Listener> configuration) {
    this.listeners = configuration;
  }

  public HealthCheck(Listener... listeners) {
    this.listeners = Arrays.asList(listeners);
  }

  public static void main(String[] args) {
    HealthCheck check = new HealthCheck(new ServerConfiguration().getListeners());
    Integer exitCode = check.call();
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    return listeners.stream()
      .map(l -> String.format("%s://127.0.0.1:%d%s/api/v2", l.getScheme(), l.getPort(), contextPath(l)))
      .mapToInt(this::checkUrl)
      .max()
      .orElse(0);
  }

  private String contextPath(Listener listener) {
    if ("/".equals(listener.getContextPath())) {
      return "";
    }
    return listener.getContextPath();
  }

  private Integer checkUrl(String url) {
    return checkUrl(url, true);
  }

  private int checkUrl(String url, boolean followRedirect) {
    try {
      HttpURLConnection connection = createConnection(url);
      int code = connection.getResponseCode();
      if (isRedirect(code) && followRedirect) {
        String location = connection.getHeaderField("Location");
        if (location != null && !location.isEmpty()) {
          return checkUrl(location, false);
        } else {
          return 1;
        }
      }
      return code == 200 ? 0 : 1;
    } catch (IOException e) {
      return 2;
    }
  }

  private boolean isRedirect(int code) {
    return code == HttpServletResponse.SC_MOVED_PERMANENTLY
      || code == HttpServletResponse.SC_MOVED_TEMPORARILY // same as SC_FOUND
      || code == HttpServletResponse.SC_SEE_OTHER
      || code == HttpServletResponse.SC_TEMPORARY_REDIRECT
      || code == 308; // could not find SC field
  }

  private HttpURLConnection createConnection(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(15000);
    connection.setRequestProperty("User-Agent", "scm-health-check/1.0");
    if (connection instanceof HttpsURLConnection) {
      applyHttpsConfiguration((HttpsURLConnection) connection);
    }
    return connection;
  }

  @SuppressWarnings("java:S5527")
  private void applyHttpsConfiguration(HttpsURLConnection connection) {
    connection.setHostnameVerifier((hostname, session) -> true);
    SSLSocketFactory socketFactory = createSslSocketFactory();
    if (socketFactory != null) {
      connection.setSSLSocketFactory(socketFactory);
    }
  }

  private SSLSocketFactory createSslSocketFactory() {
    try {
      SSLContext context = SSLContext.getInstance("TLSv1.2");
      context.init(null, new X509TrustManager[]{new TrustAllTrustManager()}, null);
      return context.getSocketFactory();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      return null;
    }
  }

  @SuppressWarnings("java:S4830")
  private static class TrustAllTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
      // accept anything
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
      // accept anything
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }
}
