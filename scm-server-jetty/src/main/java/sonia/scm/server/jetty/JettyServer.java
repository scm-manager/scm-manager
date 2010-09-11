/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    //server.setStopAtShutdown(true);

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
