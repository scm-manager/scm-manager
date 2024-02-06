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
