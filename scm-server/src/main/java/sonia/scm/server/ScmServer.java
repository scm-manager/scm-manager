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



package sonia.scm.server;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.xml.XmlConfiguration;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServer extends Thread
{

  /** Field description */
  public static final String CONFIGURATION = "/server-config.xml";

  /** Field description */
  static final int GRACEFUL_TIMEOUT = 2000;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ScmServer()
  {
    URL configURL = ScmServer.class.getResource(CONFIGURATION);

    if (configURL == null)
    {
      throw new ScmServerException("could not find server-config.xml");
    }

    server = new org.eclipse.jetty.server.Server();

    try
    {
      XmlConfiguration config = new XmlConfiguration(configURL);

      config.configure(server);
    }
    catch (Exception ex)
    {
      throw new ScmServerException("error during server configuration", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void run()
  {
    try
    {
      if (!initialized)
      {
        init();
      }

      server.join();
    }
    catch (InterruptedException ex)
    {
      throw new ScmServerException("could not start scm-server", ex);
    }
  }

  /**
   * Stop embedded webserver. Use {@link Server#stop()} to fix windows service.
   *
   * @see <a href="http://goo.gl/Zfy0Ev">http://goo.gl/Zfy0Ev</a>
   */
  public void stopServer()
  {
    try
    {
      server.setStopTimeout(GRACEFUL_TIMEOUT);
      server.setStopAtShutdown(true);
      server.stop();
      initialized = false;
    }
    catch (Exception ex)
    {
      ex.printStackTrace(System.err);
    }
  }

  /**
   * Method description
   *
   */
  void init()
  {
    try
    {
      server.start();
      initialized = true;
    }
    catch (Exception ex)
    {
      throw new ScmServerException("could not initialize server", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean initialized = false;

  /** Field description */
  private Server server = new Server();
}
