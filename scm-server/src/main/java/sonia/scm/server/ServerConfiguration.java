/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.server;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ServerConfiguration {

  @SuppressWarnings("java:S1075") // not a real uri
  private static final String DEFAULT_CONTEXT_PATH = "/scm";
  private final ServerConfigYaml configYaml;
  private final String baseDir = System.getProperty("basedir", ".");

  public ServerConfiguration() {
    this.configYaml = new ServerConfigParser().parse();
  }

  // Visible for testing
  public ServerConfiguration(URL configFile) {
    this.configYaml = new ServerConfigParser().parse(configFile);
  }

  public void configureServer(Server server) {
    try {
      configureHttp(server);
      configureHandler(server);
      if (configYaml.getHttps().getKeyStorePath() != null && !configYaml.getHttps().getKeyStorePath().isEmpty()) {
        configureSSL(server);
      }
    } catch (Exception ex) {
      throw new ScmServerException("error during server configuration", ex);
    }
  }

  private void configureSSL(Server server) {
    SslContextFactory.Server sslServer = new SslContextFactory.Server();
    ServerConfigYaml.SSLConfig https = configYaml.getHttps();
    sslServer.setKeyStorePath(https.getKeyStorePath());
    sslServer.setKeyStorePassword(https.getKeyStorePassword());
    sslServer.setKeyStoreType(https.getKeyStoreType());

    sslServer.setIncludeProtocols("TLSv1.2", "TLSv1.3");
    sslServer.setIncludeCipherSuites(
      "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
      "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
      "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
      "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
      "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
      "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
      "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256");
    sslServer.setUseCipherSuitesOrder(false);
    HttpConfiguration sslHttpConfig = new HttpConfiguration(createCustomHttpConfig());
    sslHttpConfig.addCustomizer(new SecureRequestCustomizer(
        false,
        true,
        -1,
        false
      )
    );
    ServerConnector sslConnector = new ServerConnector(server, (
      new SslConnectionFactory(sslServer, "http/1.1")),
      new HttpConnectionFactory(sslHttpConfig));
    sslConnector.setHost(configYaml.getAddressBinding());
    sslConnector.setPort(https.getSslPort());
    server.addConnector(sslConnector);
  }

  private void configureHttp(Server server) {
    HttpConfiguration httpConfig = createCustomHttpConfig();
    configureHttps(httpConfig);
    ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
    System.out.println("Set http address binding to " + configYaml.getAddressBinding());
    connector.setHost(configYaml.getAddressBinding());
    System.out.println("Set http port to " + configYaml.getPort());
    connector.setPort(configYaml.getPort());
    if (configYaml.getIdleTimeout() > 0) {
      System.out.println("Set http idle timeout to " + configYaml.getIdleTimeout());
      connector.setIdleTimeout(configYaml.getIdleTimeout());
    }
    server.addConnector(connector);
  }

  private void configureHttps(HttpConfiguration httpConfig) {
    ServerConfigYaml.SSLConfig https = configYaml.getHttps();
    httpConfig.setSecurePort(https.getSslPort());
    httpConfig.setSecureScheme("https");
  }

  private void configureHandler(Server server) {
    HandlerCollection handlerCollection = new HandlerCollection();
    if (configYaml.getHttps().isRedirectHttpToHttps()) {
      System.out.println("Set http to https redirect");
      handlerCollection.addHandler(new SecuredRedirectHandler(HttpStatus.MOVED_PERMANENTLY_301));
    }
    handlerCollection.addHandler(createWebAppContext());
    handlerCollection.addHandler(createDocRoot());
    server.setHandler(handlerCollection);
  }

  private WebAppContext createWebAppContext() {
    WebAppContext webApp = new WebAppContext();
    webApp.setContextPath(configYaml.getContextPath());
    // disable directory listings
    webApp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

    String baseDir = this.baseDir;
    String warFile = asCanonicalFile(Paths.get(baseDir, "/var/webapp/scm-webapp.war")).toString();
    System.out.println("Set webapp war file to " + warFile);
    webApp.setWar(warFile);
    String tempDir = configYaml.getTempDir();
    File webappTempDir =
      asCanonicalFile(tempDir.startsWith("/") ?
        Paths.get(tempDir, "webapp") :
        Paths.get(baseDir, tempDir, "scm")
      );
    System.out.printf("Set webapp temp directory to %s%n", webappTempDir);
    webApp.setTempDirectory(webappTempDir);
    webApp.setMaxFormContentSize(configYaml.getMaxFormContentSize());
    System.out.println("Set webapp max form content size to " + configYaml.getMaxFormContentSize());
    webApp.setMaxFormKeys(configYaml.getMaxFormKeys());
    System.out.println("Set webapp max form keys to " + configYaml.getMaxFormKeys());
    return webApp;
  }

  private WebAppContext createDocRoot() {
    WebAppContext docRoot = new WebAppContext();
    docRoot.setContextPath("/");
    String baseDir = this.baseDir;
    docRoot.setBaseResource(new ResourceCollection(new String[]{baseDir + "/var/webapp/docroot"}));
    String tempDir = configYaml.getTempDir();
    File docRootTempDir =
      asCanonicalFile(tempDir.startsWith("/") ?
        Paths.get(tempDir, "work/docroot") :
        Paths.get(baseDir, tempDir, "docroot")
      );
    System.out.printf("Set docroot temp directory to %s%n", docRootTempDir);
    docRoot.setTempDirectory(docRootTempDir);
    return docRoot;
  }

  private File asCanonicalFile(Path path) {
    try {
      return path.toFile().getAbsoluteFile().getCanonicalFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private HttpConfiguration createCustomHttpConfig() {
    HttpConfiguration httpConfig = new HttpConfiguration();
    System.out.println("Set http request header size to " + configYaml.getHttpHeaderSize());
    httpConfig.setRequestHeaderSize(configYaml.getHttpHeaderSize());
    System.out.println("Set http response header size to " + configYaml.getHttpHeaderSize());
    httpConfig.setResponseHeaderSize(configYaml.getHttpHeaderSize());
    httpConfig.setSendServerVersion(false);
    System.out.println("Set forward request customizer: " + configYaml.isForwardHeadersEnabled());
    if (configYaml.isForwardHeadersEnabled()) {
      httpConfig.addCustomizer(new ForwardedRequestCustomizer());
    }
    return httpConfig;
  }

  public List<Listener> getListeners() {
    List<Listener> listeners = new ArrayList<>();

    Server server = new Server();
    configureServer(server);

    String contextPath = findContextPath(server.getHandlers());
    if (contextPath == null) {
      contextPath = DEFAULT_CONTEXT_PATH;
    }

    for (Connector connector : server.getConnectors()) {
      if (connector instanceof ServerConnector serverConnector) {
        String scheme = "http";
        String protocol = serverConnector.getDefaultProtocol();
        if ("SSL".equalsIgnoreCase(protocol) || "TLS".equalsIgnoreCase(protocol)) {
          scheme = "https";
        }
        listeners.add(new Listener(scheme, serverConnector.getPort(), contextPath));
      }
    }

    return listeners;
  }

  private String findContextPath(Handler[] handlers) {
    for (Handler handler : handlers) {
      if (handler instanceof WebAppContext) {
        return ((WebAppContext) handler).getContextPath();
      } else if (handler instanceof HandlerCollection) {
        String contextPath = findContextPath(((HandlerCollection) handler).getHandlers());
        if (contextPath != null) {
          return contextPath;
        }
      }
    }
    return null;
  }
}
