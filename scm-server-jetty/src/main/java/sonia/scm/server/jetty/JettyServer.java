/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */

package sonia.scm.server.jetty;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import sonia.scm.server.Server;
import sonia.scm.server.ServerAllreadyRunningException;
import sonia.scm.server.ServerConfig;
import sonia.scm.server.ServerException;
import sonia.scm.server.ServerListener;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class JettyServer implements Server
{

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(ServerListener listener)
  {
    listeners.add(listener);
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(ServerListener listener)
  {
    listeners.remove(listener);
  }

  /**
   * Method description
   *
   *
   * @param config
   * @param webapp
   *
   * @throws IOException
   * @throws ServerException
   */
  @Override
  public void start(ServerConfig config, File webapp)
          throws ServerException, IOException
  {
    if (isRunning())
    {
      throw new ServerAllreadyRunningException();
    }

    server = new org.eclipse.jetty.server.Server();

    Connector connector = new SelectChannelConnector();

    for (ServerListener listener : listeners)
    {
      connector.addLifeCycleListener(new JettyServerListenerAdapter(listener));
    }

    connector.setPort(config.getPort());
    server.addConnector(connector);

    WebAppContext wac = new WebAppContext();

    wac.setContextPath(config.getContextPath());
    wac.setWar(webapp.getAbsolutePath());
    wac.setExtractWAR(true);
    server.setHandler(wac);

    // server.setStopAtShutdown(true);

    try
    {
      server.start();
      server.join();
    }
    catch (Exception ex)
    {
      throw new ServerException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ServerException
   */
  @Override
  public void stop() throws ServerException, IOException
  {
    if (isRunning())
    {
      try
      {
        server.stop();
      }
      catch (Exception ex)
      {
        throw new ServerException(ex);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isRunning()
  {
    return server != null;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<ServerListener> listeners = new HashSet<ServerListener>();

  /** Field description */
  private org.eclipse.jetty.server.Server server;
}
