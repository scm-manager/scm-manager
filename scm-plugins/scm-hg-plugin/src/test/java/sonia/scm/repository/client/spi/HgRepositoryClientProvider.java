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

import org.javahg.Repository;
import com.google.common.collect.ImmutableSet;
import sonia.scm.repository.client.api.ClientCommand;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Mercurial implementation of the {@link RepositoryClientProvider}.
 * 
 */
public class HgRepositoryClientProvider extends RepositoryClientProvider
{
  
  private static final Set<ClientCommand> SUPPORTED_COMMANDS = ImmutableSet.of(
    ClientCommand.ADD, ClientCommand.REMOVE, ClientCommand.COMMIT, 
    ClientCommand.TAG, ClientCommand.BRANCH, ClientCommand.PUSH
  );
  
  private final Repository repository;
  private final File hgrc;
  private final String url;

  HgRepositoryClientProvider(Repository repository, File hgrc, String url)
  {
    this.repository = repository;
    this.hgrc = hgrc;
    this.url = url;
  }

  @Override
  public Set<ClientCommand> getSupportedClientCommands()
  {
    return SUPPORTED_COMMANDS;
  }

  @Override
  public AddCommand getAddCommand()
  {
    return new HgAddCommand(repository);
  }

  @Override
  public RemoveCommand getRemoveCommand()
  {
    return new HgRemoveCommand(repository);
  }

  @Override
  public CommitCommand getCommitCommand()
  {
    return new HgCommitCommand(repository);
  }

  @Override
  public TagCommand getTagCommand()
  {
    return new HgTagCommand(repository);
  }

  @Override
  public BranchCommand getBranchCommand()
  {
    return new HgBranchCommand(repository);
  }

  @Override
  public PushCommand getPushCommand()
  {
    return new HgPushCommand(repository, url);
  }

  @Override
  public File getWorkingCopy() {
    return repository.getDirectory();
  }

  @Override
  public void close() throws IOException
  {
    if ( hgrc != null && hgrc.exists() && ! hgrc.delete() ){
      throw new IOException("failed to remove hgrc file ".concat(hgrc.getPath()));
    }
    repository.close();
  }
}
