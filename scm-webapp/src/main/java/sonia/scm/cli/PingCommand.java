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

package sonia.scm.cli;

import jakarta.inject.Inject;
import picocli.CommandLine;

@CommandLine.Command(name = "ping", hidden = true)
class PingCommand implements Runnable {

  private final CliContext context;

  @Inject
  public PingCommand(CliContext context) {
    this.context = context;
  }

  @Override
  public void run() {
    context.getStdout().println("PONG");
  }
}
