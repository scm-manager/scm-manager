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
