/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository.client.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.client.api.ClientCommand;

import java.io.File;
import java.io.IOException;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryClientProvider extends RepositoryClientProvider
{

  /** Field description */
  private static final Set<ClientCommand> SUPPORTED_COMMANDS =
    ImmutableSet.of(ClientCommand.ADD, ClientCommand.REMOVE,
      ClientCommand.COMMIT, ClientCommand.TAG, ClientCommand.BRANCH,
      ClientCommand.PUSH);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param git
   * @param credentialsProvider
   */
  GitRepositoryClientProvider(Git git)
  {
    this(git, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param git
   * @param credentialsProvider
   */
  GitRepositoryClientProvider(Git git, CredentialsProvider credentialsProvider)
  {
    this.git = git;
    this.credentialsProvider = credentialsProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    GitUtil.close(git.getRepository());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public AddCommand getAddCommand()
  {
    return new GitAddCommand(git);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public BranchCommand getBranchCommand()
  {
    return new GitBranchCommand(git);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public CommitCommand getCommitCommand()
  {
    return new GitCommitCommand(git);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public PushCommand getPushCommand()
  {
    return new GitPushCommand(git, credentialsProvider);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RemoveCommand getRemoveCommand()
  {
    return new GitRemoveCommand(git);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<ClientCommand> getSupportedClientCommands()
  {
    return SUPPORTED_COMMANDS;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public TagCommand getTagCommand()
  {
    return new GitTagCommand(git);
  }

  @Override
  public File getWorkingCopy() {
    return git.getRepository().getWorkTree();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CredentialsProvider credentialsProvider;

  /** Field description */
  private Git git;
}
