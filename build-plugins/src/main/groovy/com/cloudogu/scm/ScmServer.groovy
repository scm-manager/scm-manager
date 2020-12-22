/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.scm

import com.google.common.base.Strings
import groovy.json.JsonSlurper
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.webapp.WebAppContext

import java.awt.Desktop

public class ScmServer {

  def configuration
  private Server server

  private ScmServer(def configuration) {
    this.configuration = configuration
  }

  void start() throws Exception {
    info('start scm-server at port %s', configuration.port)

    System.setProperty('scm.home', configuration.home)
    if (configuration.disableCorePlugins) {
      info('disable core plugin extraction')
      System.setProperty('sonia.scm.boot.disable-core-plugin-extraction', 'true')
    }

    if (!Strings.isNullOrEmpty(configuration.loggingConfiguration)) {
      System.setProperty('logback.configurationFile', configuration.loggingConfiguration);
    }

    info('set stage %s', configuration.stage)
    System.setProperty('scm.stage', configuration.stage)

    server = new Server()
    server.addConnector(createServerConnector(server))
    server.setHandler(createScmContext())
    server.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
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

  private ServerConnector createServerConnector(Server server) throws MalformedURLException {
    ServerConnector connector = new ServerConnector(server)
    HttpConfiguration cfg = new HttpConfiguration()

    cfg.setRequestHeaderSize(configuration.headerSize)
    cfg.setResponseHeaderSize(configuration.headerSize)

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
