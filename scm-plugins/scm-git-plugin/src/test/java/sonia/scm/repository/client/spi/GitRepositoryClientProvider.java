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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.client.api.ClientCommand;

import java.io.File;
import java.io.IOException;
import java.util.Set;


public class GitRepositoryClientProvider extends RepositoryClientProvider
{

  private static final Set<ClientCommand> SUPPORTED_COMMANDS =
    ImmutableSet.of(ClientCommand.ADD, ClientCommand.REMOVE,
      ClientCommand.COMMIT, ClientCommand.TAG, ClientCommand.BRANCH,
      ClientCommand.DELETE_REMOTE_BRANCH, ClientCommand.MERGE, ClientCommand.PUSH);


 
  GitRepositoryClientProvider(Git git)
  {
    this(git, null);
  }

 
  GitRepositoryClientProvider(Git git, CredentialsProvider credentialsProvider)
  {
    this.git = git;
    this.credentialsProvider = credentialsProvider;
  }



  @Override
  public void close() throws IOException
  {
    GitUtil.close(git.getRepository());
  }


  
  @Override
  public AddCommand getAddCommand()
  {
    return new GitAddCommand(git);
  }

  
  @Override
  public BranchCommand getBranchCommand()
  {
    return new GitBranchCommand(git);
  }

  @Override
  public DeleteRemoteBranchCommand getDeleteRemoteBranchCommand() {
    return new GitDeleteRemoteBranchCommand(git, credentialsProvider);
  }

  @Override
  public CheckoutCommand getCheckoutCommand() {
    return new GitCheckoutCommand(git);
  }

  
  @Override
  public CommitCommand getCommitCommand()
  {
    return new GitCommitCommand(git);
  }

  
  @Override
  public PushCommand getPushCommand()
  {
    return new GitPushCommand(git, credentialsProvider);
  }

  
  @Override
  public RemoveCommand getRemoveCommand()
  {
    return new GitRemoveCommand(git);
  }

  
  @Override
  public Set<ClientCommand> getSupportedClientCommands()
  {
    return SUPPORTED_COMMANDS;
  }

  
  @Override
  public TagCommand getTagCommand()
  {
    return new GitTagCommand(git);
  }

  @Override
  public MergeCommand getMergeCommand() {
    return new GitMergeCommand(git);
  }

  @Override
  public File getWorkingCopy() {
    return git.getRepository().getWorkTree();
  }

  //~--- fields ---------------------------------------------------------------

  private CredentialsProvider credentialsProvider;

  private Git git;
}
