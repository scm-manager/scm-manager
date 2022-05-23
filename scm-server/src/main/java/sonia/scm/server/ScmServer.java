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

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jetty.server.Server;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServer extends Thread
{
  /** Field description */
  static final int GRACEFUL_TIMEOUT = 2000;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ScmServer()
  {
    ServerConfiguration config = new ServerConfiguration();
    server = new org.eclipse.jetty.server.Server();
    config.configure(server);
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
