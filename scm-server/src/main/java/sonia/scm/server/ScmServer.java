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

import org.eclipse.jetty.server.Server;

public class ScmServer extends Thread {
  static final int GRACEFUL_TIMEOUT = 2000;
  private boolean initialized = false;
  private final Server server;

  public ScmServer() {
    server = new org.eclipse.jetty.server.Server();
    ServerConfiguration config = new ServerConfiguration();
    config.configureServer(server);
  }

  @Override
  public void run() {
    try {
      if (!initialized) {
        init();
      }

      server.join();
    } catch (InterruptedException ex) {
      System.err.println("server interrupted");
      ex.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Stop embedded webserver. Use {@link Server#stop()} to fix windows service.
   *
   * @see <a href="http://goo.gl/Zfy0Ev">http://goo.gl/Zfy0Ev</a>
   */
  public void stopServer() {
    try {
      server.setStopTimeout(GRACEFUL_TIMEOUT);
      server.setStopAtShutdown(true);
      server.stop();
      initialized = false;
    } catch (Exception ex) {
      ex.printStackTrace(System.err);
    }
  }

  void init() {
    try {
      server.start();
      initialized = true;
    } catch (Exception ex) {
      throw new ScmServerException("could not initialize server", ex);
    }
  }
}
