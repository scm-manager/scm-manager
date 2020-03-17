/**
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

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServerDaemon implements Daemon
{

  /** Field description */
  private static volatile ScmServer webserver = new ScmServer();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   */
  public static void main(String[] args)
  {
    webserver.run();
  }

  /**
   * Method description
   *
   *
   * @param args
   *
   * @throws Exception
   */
  public static void start(String[] args) throws Exception
  {
    webserver.start();
  }

  /**
   * Method description
   *
   *
   * @param args
   *
   * @throws Exception
   */
  public static void stop(String[] args) throws Exception
  {
    webserver.stopServer();
    webserver.join((long) ScmServer.GRACEFUL_TIMEOUT);
  }

  /**
   * Method description
   *
   */
  @Override
  public void destroy()
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @throws DaemonInitException
   * @throws Exception
   */
  @Override
  public void init(DaemonContext context) throws DaemonInitException, Exception
  {
    daemonArgs = context.getArguments();

    // initialize web server and open port. We have to do this in the init
    // method, because this method is started by jsvc with super user privileges.
    webserver.init();
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  public void start() throws Exception
  {
    start(daemonArgs);
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  public void stop() throws Exception
  {
    stop(daemonArgs);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String[] daemonArgs;
}
