/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.server;


import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;


public class ScmServerDaemon implements Daemon
{

  private static volatile ScmServer webserver = new ScmServer();

  private String[] daemonArgs;

  public static void main(String[] args)
  {
    webserver.run();
  }

  
  public static void start(String[] args) throws Exception
  {
    webserver.start();
  }

  
  public static void stop(String[] args) throws Exception
  {
    webserver.stopServer();
    webserver.join((long) ScmServer.GRACEFUL_TIMEOUT);
  }

   @Override
  public void destroy()
  {

    // do nothing
  }

  @Override
  public void init(DaemonContext context) throws DaemonInitException, Exception
  {
    daemonArgs = context.getArguments();

    // initialize web server and open port. We have to do this in the init
    // method, because this method is started by jsvc with super user privileges.
    webserver.init();
  }

  @Override
  public void start() throws Exception
  {
    start(daemonArgs);
  }

  @Override
  public void stop() throws Exception
  {
    stop(daemonArgs);
  }

}
