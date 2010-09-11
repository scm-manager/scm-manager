/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.server;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public interface Server
{

  /**
   * Method description
   *
   *
   * @param listener
   */
  public void addListener(ServerListener listener);

  /**
   * Method description
   *
   *
   * @param listener
   */
  public void removeListener(ServerListener listener);

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
  public void start(ServerConfig config, File webapp)
          throws ServerException, IOException;

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ServerException
   */
  public void stop() throws ServerException, IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isRunning();
}
