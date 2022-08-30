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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ServerConfiguration {

  private static final String CONFIGURATION = "/server-config.xml";
  @SuppressWarnings("java:S1075") // not a real uri
  private static final String DEFAULT_CONTEXT_PATH = "/scm";

  private final XmlConfiguration jettyConfiguration;

  public ServerConfiguration() {
    this(CONFIGURATION);
  }

  public ServerConfiguration(String configurationUrl) {
    this.jettyConfiguration = read(configurationUrl);
  }

  public ServerConfiguration(Path configurationPath) {
    this.jettyConfiguration = parse(Resource.newResource(configurationPath));
  }

  public void configure(Server server) {
    try {
      jettyConfiguration.configure(server);
    } catch (Exception ex) {
      throw new ScmServerException("error during server configuration", ex);
    }
  }

  public List<Listener> getListeners() {
    List<Listener> listeners = new ArrayList<>();

    Server server = new Server();
    configure(server);

    String contextPath = findContextPath(server.getHandlers());
    if (contextPath == null) {
      contextPath = DEFAULT_CONTEXT_PATH;
    }

    for (Connector connector : server.getConnectors()) {
      if (connector instanceof ServerConnector) {
        ServerConnector serverConnector = (ServerConnector) connector;
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

  private static XmlConfiguration read(String configurationUrl) {
    URL configURL = ScmServer.class.getResource(configurationUrl);

    if (configURL == null) {
      throw new ScmServerException("could not find server-config.xml");
    }

    return parse(Resource.newResource(configURL));
  }

  private static XmlConfiguration parse(Resource resource) {
    try {
      return new XmlConfiguration(resource);
    } catch (IOException | SAXException ex) {
      throw new ScmServerException("could not read server configuration", ex);
    }
  }

}
