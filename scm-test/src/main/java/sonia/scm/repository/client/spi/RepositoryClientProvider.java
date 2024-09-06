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

package sonia.scm.repository.client.spi;


import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.ClientCommandNotSupportedException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @since 1.18
 */
public abstract class RepositoryClientProvider implements Closeable
{

  
  public abstract Set<ClientCommand> getSupportedClientCommands();



  @Override
  public void close() throws IOException {}


  
  public AddCommand getAddCommand()
  {
    throw new ClientCommandNotSupportedException(ClientCommand.ADD);
  }

  
  public BranchCommand getBranchCommand()
  {
    throw new ClientCommandNotSupportedException(ClientCommand.BRANCH);
  }

  public DeleteRemoteBranchCommand getDeleteRemoteBranchCommand() {
    throw new ClientCommandNotSupportedException(ClientCommand.DELETE_REMOTE_BRANCH);
  }

  public CheckoutCommand getCheckoutCommand() {
    throw new ClientCommandNotSupportedException(ClientCommand.CHECKOUT);
  }

  
  public CommitCommand getCommitCommand()
  {
    throw new ClientCommandNotSupportedException(ClientCommand.COMMIT);
  }

  
  public PushCommand getPushCommand()
  {
    throw new ClientCommandNotSupportedException(ClientCommand.PUSH);
  }

  
  public RemoveCommand getRemoveCommand()
  {
    throw new ClientCommandNotSupportedException(ClientCommand.REMOVE);
  }

  
  public TagCommand getTagCommand()
  {
    throw new ClientCommandNotSupportedException(ClientCommand.TAG);
  }

  public MergeCommand getMergeCommand() {
    throw new ClientCommandNotSupportedException(ClientCommand.MERGE);
  }

  /**
   * Returns the working copy of the repository client.
   *
   * @return working copy
   * @since 1.51
   */
  public abstract File getWorkingCopy();
}
