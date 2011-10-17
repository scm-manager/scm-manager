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



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.IOUtils;

import org.eclipse.jetty.server.Server;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Sebastian Sdorra
 */
public class JettyStopMonitorThread extends Thread
{

  /** Field description */
  public static final String ADDRESS_LOCALHOST = "127.0.0.1";

  /** Field description */
  public static final String NAME = "JettyStopMonitor";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param server
   * @param stopPort
   * @param stopKey
   */
  public JettyStopMonitorThread(Server server, int stopPort, String stopKey)
  {
    this.server = server;
    this.stopKey = stopKey;
    setDaemon(true);
    setName(NAME);

    try
    {
      socket = new ServerSocket(stopPort, 1,
                                InetAddress.getByName(ADDRESS_LOCALHOST));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
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
    BufferedReader reader = null;
    Socket accept = null;

    try
    {
      accept = socket.accept();
      reader =
        new BufferedReader(new InputStreamReader(accept.getInputStream()));

      String line = reader.readLine();

      if (stopKey.equals(line))
      {
        server.stop();
        socket.close();
      }

      accept.close();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      IOUtils.closeQuietly(reader);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Server server;

  /** Field description */
  private ServerSocket socket;

  /** Field description */
  private String stopKey;
}
