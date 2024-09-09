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

package com.cloudogu.scm

import com.google.common.base.Strings
import groovy.json.JsonSlurper
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ShutdownHandler
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.webapp.WebAppContext

import java.awt.Desktop

class ScmServer {

  def configuration
  private Server server

  private ScmServer(def configuration) {
    this.configuration = configuration
  }

  void start() throws Exception {
    info('start scm-server at port %s', configuration.port)

    System.setProperty("jakarta.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

    if (configuration.disableCorePlugins) {
      info('disable core plugin extraction')
      System.setProperty('sonia.scm.boot.disable-core-plugin-extraction', 'true')
    }

    info('set stage %s', configuration.stage)
    System.setProperty('scm.stage', configuration.stage)

    if (!Strings.isNullOrEmpty(configuration.livereloadUrl)) {
      info('set livereload url', configuration.livereloadUrl)
      System.setProperty('sonia.scm.ui.proxy', configuration.livereloadUrl)
    }

    server = new Server()
    server.addConnector(createServerConnector(server))

    HandlerList handlerList = new HandlerList()
    handlerList.setHandlers([
      createScmContext(),
      createShutdownHandler()
    ] as Handler[])
    server.setHandler(handlerList)
    server.addEventListener(new LifeCycle.Listener() {
      @Override
      void lifeCycleStarted(LifeCycle event) {

        String endpoint = String.format('http://localhost:%d%s', configuration.port, configuration.contextPath)

        System.out.println()
        System.out.println('==> scm-server started successfully and is accessible at:')
        System.out.append('==> ').println(endpoint)
        System.out.println()

        if (configuration.openBrowser) {
          openBrowser(endpoint)
        }
      }
    })

    server.start()
  }

  private static void openBrowser(String endpoint) {
    try {
      Desktop desktop = Desktop.getDesktop()
      desktop.browse(URI.create(endpoint))
    } catch (IOException | URISyntaxException ex) {
      warn('could not open browser', ex)
    }
  }

  private static void info(String message, Object... args) {
    log('INFO', message, args)
  }

  private static void warn(String message, Exception exception) {
    log('WARN', message)
    exception.printStackTrace(System.out)
  }

  private static void log(String level, String template, Object... args) {
    System.out.println("[${level}] " + String.format(template, args))
  }

  private WebAppContext createScmContext() {
    WebAppContext warContext = new WebAppContext()

    warContext.setContextPath(configuration.contextPath)
    warContext.setExtractWAR(true)
    warContext.setWar(configuration.warFile)

    return warContext
  }

  private ShutdownHandler createShutdownHandler() {
    return new ShutdownHandler("_shutdown_", true, false)
  }

  private ServerConnector createServerConnector(Server server) throws MalformedURLException {
    ServerConnector connector = new ServerConnector(server)
    HttpConfiguration cfg = new HttpConfiguration()

    cfg.setRequestHeaderSize(configuration.headerSize)
    cfg.setResponseHeaderSize(configuration.headerSize)
    cfg.setSendServerVersion(false)

    connector.setConnectionFactories([new HttpConnectionFactory(cfg)])
    connector.setPort(configuration.port)

    return connector
  }

  static void main(String[] args) {
    String configurationPath = args[0]
    JsonSlurper slurper = new JsonSlurper()
    def configuration = slurper.parse(new File(configurationPath))
    ScmServer server = new ScmServer(configuration)
    server.start()
  }

}
