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
