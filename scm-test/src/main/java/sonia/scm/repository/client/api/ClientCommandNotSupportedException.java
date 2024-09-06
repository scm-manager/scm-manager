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

package sonia.scm.repository.client.api;

/**
 *
 * @since 1.18
 */
public class ClientCommandNotSupportedException extends RuntimeException
{
  private ClientCommand clientCommand;
 
  public ClientCommandNotSupportedException(ClientCommand clientCommand)
  {
    super(
      "client command ".concat(clientCommand.name()).concat(
        " is not supported"));
    this.clientCommand = clientCommand;
  }

 
  public ClientCommandNotSupportedException(ClientCommand clientCommand,
    String message)
  {
    super(message);
    this.clientCommand = clientCommand;
  }


  
  public ClientCommand getClientCommand()
  {
    return clientCommand;
  }

}
