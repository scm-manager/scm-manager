/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.agent;

//~--- JDK imports ------------------------------------------------------------

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import java.io.IOException;

import java.text.MessageFormat;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class Main
{

  /** Field description */
  private static final int DEFAULT_PORT = 8989;

  /** Field description */
  private static final String DEFAULT_URI = "http://localhost:{0}/";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   *
   * @throws IOException
   */
  public static void main(String[] args) throws IOException
  {
    SelectorThread threadSelector = startServer();

    // TODO replace
    System.in.read();
    System.out.println("Shutting down ...");
    threadSelector.stopEndpoint();
    System.exit(0);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  protected static SelectorThread startServer() throws IOException
  {
    final Map<String, String> initParams = new HashMap<String, String>();

    initParams.put("com.sun.jersey.config.property.packages",
                   "sonia.scm.agent.resources");
    System.out.println("Starting grizzly...");

    int port = getPort(DEFAULT_PORT);
    String uri = MessageFormat.format(DEFAULT_URI, String.valueOf(port));

    return GrizzlyWebContainerFactory.create(uri, initParams);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param defaultPort
   *
   * @return
   */
  private static int getPort(int defaultPort)
  {
    String port = System.getenv("JERSEY_HTTP_PORT");

    if (null != port)
    {
      try
      {
        return Integer.parseInt(port);
      }
      catch (NumberFormatException e) {}
    }

    return defaultPort;
  }
}
