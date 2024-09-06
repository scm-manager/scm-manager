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
