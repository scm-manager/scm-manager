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

package sonia.scm.io;

import java.io.IOException;

import java.util.Timer;


public class ExtendedCommand extends SimpleCommand
{
  private long timeout = 30000;

  public ExtendedCommand(String... command)
  {
    super(command);
  }

  @Override
  public SimpleCommandResult execute() throws IOException
  {
    SimpleCommandResult result = null;
    Process process = createProcess();
    Timer timer = new Timer();
    ProcessInterruptScheduler pis = null;

    try
    {
      pis = new ProcessInterruptScheduler(process);
      timer.schedule(pis, timeout);
      result = getResult(process);
    }
    finally
    {
      timer.cancel();
    }

    return result;
  }

  public long getTimeout()
  {
    return timeout;
  }

  public void setTimeout(long timeout)
  {
    this.timeout = timeout;
  }

}
