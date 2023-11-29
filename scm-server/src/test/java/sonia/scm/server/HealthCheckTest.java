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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealthCheckTest {

  private List<Server> servers;

  @Test
  void shouldReturnZero() throws Exception {
    int port = startHttpServer("/scm");

    HealthCheck check = new HealthCheck(new Listener("http", port, "/scm"));
    assertThat(check.call()).isZero();
  }

  @Test
  void shouldReturnNonZeroForWrongContextPath() throws Exception {
    int port = startHttpServer("/scm");
    HealthCheck check = new HealthCheck(new Listener("http", port, "/myscm"));
    assertThat(check.call()).isPositive();
  }

  @Test
  void shouldReturnNonZeroForWrongPort() throws Exception {
    int port = startHttpServer("/scm");
    HealthCheck check = new HealthCheck(new Listener("http", port + 1, "/scm"));
    assertThat(check.call()).isPositive();
  }

  @Test
  void shouldReturnZeroForMultipleListeners() throws Exception {
    int one = startHttpServer("/scm");
    int two = startHttpServer("/scm");

    HealthCheck check = new HealthCheck(
      new Listener("http", one, "/scm"),
      new Listener("http", two, "/scm")
    );

    assertThat(check.call()).isZero();
  }

  @Test
  void shouldReturnNonZeroIfOneFails() throws Exception {
    int one = startHttpServer("/myscm");
    int two = startHttpServer("/scm");

    HealthCheck check = new HealthCheck(
      new Listener("http", one, "/myscm"),
      new Listener("http", two, "/myscm")
    );

    assertThat(check.call()).isPositive();
  }

  @Test
  void shouldHandleHttps(@TempDir Path directory) throws Exception {
    int port = startHttpsServer(directory, "/scm");

    HealthCheck check = new HealthCheck(new Listener("https", port, "/scm"));
    assertThat(check.call()).isZero();
  }

  @Test
  void shouldHandleHttpAndHttps(@TempDir Path directory) throws Exception {
    int http = startHttpServer("/scm");
    int https = startHttpsServer(directory, "/scm");

    HealthCheck check = new HealthCheck(
      new Listener("http", http, "/scm"),
      new Listener("https", https, "/scm")
    );
    assertThat(check.call()).isZero();
  }

  @Test
  void shouldFollowRedirect() throws Exception {
    int http = startHttpServer("/scm");
    int redirector = startHttpRedirector("/scm", "http", http);

    HealthCheck check = new HealthCheck(
      new Listener("http", redirector, "/scm")
    );
    assertThat(check.call()).isZero();
  }

  @Test
  void shouldFailWithInvalidRedirect() throws Exception {
    int redirector = startHttpRedirector("/scm", "http", 9999);

    HealthCheck check = new HealthCheck(
      new Listener("http", redirector, "/scm")
    );
    assertThat(check.call()).isPositive();
  }

  @Test
  void shouldFailOnRedirectWithoutLocation() throws Exception {
    int redirector = startInvalidRedirector("/scm");

    HealthCheck check = new HealthCheck(
      new Listener("http", redirector, "/scm")
    );
    assertThat(check.call()).isPositive();
  }

  @Test
  void shouldFollowRedirectFromHttpToHttps(@TempDir Path directory) throws Exception {
    int https = startHttpsServer(directory,"/scm");
    int redirector = startHttpRedirector("/scm", "https", https);

    HealthCheck check = new HealthCheck(
      new Listener("http", redirector, "/scm")
    );
    assertThat(check.call()).isZero();
  }

  @BeforeEach
  void setUp() {
    servers = new ArrayList<>();
  }

  @AfterEach
  void shutdown() {
    for (Server server : servers) {
      try {
        server.stop();
      } catch (Exception e) {
        // do nothing
      }
    }
  }

  private int startHttpServer(String contextPath) throws Exception {
    Server server = new Server(0);
    return start(contextPath, server);
  }

  private int start(String contextPath, Server server) throws Exception {
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/plain; charset=utf-8");
        if (target.equals(contextPath + "/api/v2")) {
          response.setStatus(HttpServletResponse.SC_OK);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        baseRequest.setHandled(true);
      }
    });


    server.start();
    servers.add(server);
    return server.getURI().getPort();
  }

  private int startHttpsServer(Path directory, String contextPath) throws Exception {
    Server server = new Server();

    ServerConnector sslConnector = createSslConnector(server, directory, "changeit");
    server.addConnector(sslConnector);

    return start(contextPath, server);
  }

  private int startHttpRedirector(String contextPath, String targetScheme, int targetPort) throws Exception {
    Server server = new Server(0);
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/plain; charset=utf-8");
        if (target.equals(contextPath + "/api/v2")) {
          response.setHeader("Location", String.format("%s://127.0.0.1:%d%s", targetScheme, targetPort, target));
          response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        baseRequest.setHandled(true);
      }
    });


    server.start();
    servers.add(server);
    return server.getURI().getPort();
  }

  private int startInvalidRedirector(String contextPath) throws Exception {
    Server server = new Server(0);
    server.setHandler(new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/plain; charset=utf-8");
        if (target.equals(contextPath + "/api/v2")) {
          response.setStatus(HttpServletResponse.SC_FOUND);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        baseRequest.setHandled(true);
      }
    });


    server.start();
    servers.add(server);
    return server.getURI().getPort();
  }

  private ServerConnector createSslConnector(Server server, Path directory, String password) throws Exception {
    Path keystorePath = createSelfSignedKeyStore(directory, password);
    KeyStore keyStore = createKeyStore(keystorePath, password);
    SslContextFactory.Server sslContextFactory = createSslContextFactory(keyStore, password);

    ServerConnector sslConnector = new ServerConnector(
      server,
      new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
      new HttpConnectionFactory(new HttpConfiguration())
    );

    sslConnector.setPort(0);

    return sslConnector;
  }

  private SslContextFactory.Server createSslContextFactory(KeyStore keyStore, String password) {
    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
    sslContextFactory.setKeyStore(keyStore);
    sslContextFactory.setKeyStorePassword(password);
    return sslContextFactory;
  }

  private KeyStore createKeyStore(Path keystorePath, String password) throws Exception {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (InputStream stream = Files.newInputStream(keystorePath)) {
      keyStore.load(stream, password.toCharArray());
    }
    return keyStore;
  }

  private Path createSelfSignedKeyStore(Path directory, String password) throws IOException, InterruptedException {
    // no way to create a self-signed certificate from an api, so we use keytool for now
    int result = new ProcessBuilder("keytool",
      "-genkey",
      "-keyalg", "RSA",
      "-alias", "selfsigned",
      "-keystore", "keystore",
      "-storepass", password,
      "-validity", "360",
      "-keysize", "1024",
      "-dname", "CN=127.0.0.1"
    )
      .directory(directory.toFile())
      .start()
      .waitFor();
    if (result != 0) {
      throw new IOException("failed to generate self signed certificate");
    }
    return directory.resolve("keystore");
  }
}
