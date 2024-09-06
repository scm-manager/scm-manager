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

package sonia.scm.repository.api;

/**
 * This exception is thrown if the {@link RepositoryService} does not support
 * the requested command.
 *
 * @since 1.17
 */
public final class CommandNotSupportedException extends RuntimeException
{
  /** not supported command */
  private Command command;

  public CommandNotSupportedException(Command command)
  {
    //J-
    this(
      command,
      "The requested feature "
      .concat(command.toString())
      .concat(" is not supported")
    );
    //J+
  }

  public CommandNotSupportedException(Command command, String message)
  {
    super(message);
    this.command = command;
  }


  /**
   * Returns the command which is not supported.
   */
  public Command getCommand()
  {
    return command;
  }

}
