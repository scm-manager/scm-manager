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

import com.google.common.collect.ImmutableSet;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import sonia.scm.repository.client.api.ClientCommand;

import java.io.File;
import java.util.Set;

/**
 * Subversion repository client provider.
 * 
 * @since 1.51
 */
public class SvnRepositoryClientProvider extends RepositoryClientProvider {

  private static final Set<ClientCommand> SUPPORTED_COMMANDS = ImmutableSet.of(
    ClientCommand.ADD, ClientCommand.REMOVE, ClientCommand.COMMIT
  );
  
  private final SVNClientManager client;
  private final File workingCopy;

  private final SvnChangeWorker changeWorker;

  SvnRepositoryClientProvider(SVNClientManager client, File workingCopy) {
    changeWorker = new SvnChangeWorker(workingCopy);
    this.client = client;
    this.workingCopy = workingCopy;
  }

  @Override
  public AddCommand getAddCommand() {
    return changeWorker.addCommand();
  }

  @Override
  public RemoveCommand getRemoveCommand() {
    return changeWorker.removeCommand();
  }

  @Override
  public CommitCommand getCommitCommand() {
    return changeWorker.commitCommand(client);
  }

  @Override
  public File getWorkingCopy() {
    return workingCopy;
  }
  
  @Override
  public Set<ClientCommand> getSupportedClientCommands() {
    return SUPPORTED_COMMANDS;
  }
  
}
