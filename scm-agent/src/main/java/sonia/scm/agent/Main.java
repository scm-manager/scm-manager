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

package sonia.scm.agent;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /** Field description */
  private static Logger logger = LoggerFactory.getLogger(Main.class);

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
      catch (NumberFormatException ex)
      {
        logger.debug(ex.getMessage(), ex);
      }
    }

    return defaultPort;
  }
}
