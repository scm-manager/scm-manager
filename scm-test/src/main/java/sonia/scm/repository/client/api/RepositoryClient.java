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

package sonia.scm.repository.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.client.spi.RepositoryClientProvider;
import sonia.scm.util.IOUtil;

import java.io.Closeable;
import java.io.File;

public final class RepositoryClient implements Closeable {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryClient.class);

  private final RepositoryClientProvider clientProvider;

  RepositoryClient(RepositoryClientProvider clientProvider)
  {
    this.clientProvider = clientProvider;
  }

  @Override
  public void close() {
    logger.trace("close client provider");

    IOUtil.close(clientProvider);
  }

  public AddCommandBuilder getAddCommand() {
    logger.trace("create add command");

    return new AddCommandBuilder(clientProvider.getAddCommand());
  }

  public BranchCommandBuilder getBranchCommand() {
    logger.trace("create branch command");

    return new BranchCommandBuilder(clientProvider.getBranchCommand());
  }

  public DeleteRemoteBranchCommandBuilder getDeleteRemoteBranchCommand() {
    logger.trace("delete branch command");

    return new DeleteRemoteBranchCommandBuilder(clientProvider.getDeleteRemoteBranchCommand());
  }

  public CheckoutCommandBuilder getCheckoutCommand() {
    logger.trace("create checkout command");

    return new CheckoutCommandBuilder(clientProvider.getCheckoutCommand());
  }

  public CommitCommandBuilder getCommitCommand() {
    logger.trace("create commit command");

    return new CommitCommandBuilder(clientProvider.getCommitCommand());
  }

  public PushCommandBuilder getPushCommand() {
    logger.trace("create push command");

    return new PushCommandBuilder(clientProvider.getPushCommand());
  }

  public RemoveCommandBuilder getRemoveCommand() {
    logger.trace("create remove command");

    return new RemoveCommandBuilder(clientProvider.getRemoveCommand());
  }

  public TagCommandBuilder getTagCommand() {
    logger.trace("create tag command");

    return new TagCommandBuilder(clientProvider.getTagCommand());
  }

  public MergeCommandBuilder getMergeCommand() {
    logger.trace("create merge command");

    return new MergeCommandBuilder(clientProvider.getMergeCommand());
  }

  public File getWorkingCopy() {
    return clientProvider.getWorkingCopy();
  }

  public boolean isCommandSupported(ClientCommand command) {
    return clientProvider.getSupportedClientCommands().contains(command);
  }

}
