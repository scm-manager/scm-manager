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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;

import java.net.InetAddress;
import java.net.Socket;

/**
 * @goal stop
 * @author Sebastian Sdorra
 */
public class StopMojo extends AbstractMojo
{

  /** Field description */
  public static final String ADDRESS_LOCALHOST = "127.0.0.1";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    Socket socket = null;
    PrintWriter writer = null;

    try
    {
      socket = new Socket(InetAddress.getByName(ADDRESS_LOCALHOST), stopPort);
      writer = new PrintWriter(socket.getOutputStream());
      writer.println(stopKey);
      writer.flush();
    }
    catch (Exception ex)
    {
      getLog().warn("could not stop jetty", ex);
    }
    finally
    {
      IOUtils.closeQuietly(writer);

      if (socket != null)
      {
        try
        {
          socket.close();
        }
        catch (IOException ex)
        {

          // ignore ex
        }
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
  public String getStopKey()
  {
    return stopKey;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getStopPort()
  {
    return stopPort;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stopKey
   */
  public void setStopKey(String stopKey)
  {
    this.stopKey = stopKey;
  }

  /**
   * Method description
   *
   *
   * @param stopPort
   */
  public void setStopPort(int stopPort)
  {
    this.stopPort = stopPort;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * @parameter
   */
  private String stopKey = "stop";

  /**
   * @parameter
   */
  private int stopPort = 8085;
}
