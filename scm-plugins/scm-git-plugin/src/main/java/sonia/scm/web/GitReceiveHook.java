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

package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.spi.GitHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitReceiveHook implements PreReceiveHook, PostReceiveHook
{

  /** the logger for GitReceiveHook */
  private static final Logger logger =
    LoggerFactory.getLogger(GitReceiveHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param hookEventFacade
   * @param handler
   */
  public GitReceiveHook(HookEventFacade hookEventFacade,
    GitRepositoryHandler handler)
  {
    this.hookEventFacade = hookEventFacade;
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param rpack
   * @param receiveCommands
   */
  @Override
  public void onPostReceive(ReceivePack rpack,
    Collection<ReceiveCommand> receiveCommands)
  {
    onReceive(rpack, receiveCommands, RepositoryHookType.POST_RECEIVE);
  }

  /**
   * Method description
   *
   *
   *
   * @param rpack
   * @param receiveCommands
   */
  @Override
  public void onPreReceive(ReceivePack rpack,
    Collection<ReceiveCommand> receiveCommands)
  {
    onReceive(rpack, receiveCommands, RepositoryHookType.PRE_RECEIVE);
  }

  /**
   * Method description
   *
   *
   * @param rpack
   * @param receiveCommands
   * @param type
   */
  private void handleReceiveCommands(ReceivePack rpack,
    List<ReceiveCommand> receiveCommands, RepositoryHookType type)
  {
    try
    {
      Repository repository = rpack.getRepository();
      String repositoryId = resolveRepositoryId(repository);

      logger.trace("resolved repository to {}", repositoryId);

      GitHookContextProvider context = new GitHookContextProvider(rpack, receiveCommands, repository, repositoryId);

      hookEventFacade.handle(repositoryId).fireHookEvent(type, context);

    }
    catch (Exception ex)
    {
      logger.error("could not handle receive commands", ex);

      GitHooks.abortIfPossible(type, rpack, receiveCommands, ex.getMessage());
    }
  }

  /**
   * Method description
   *
   *
   * @param rpack
   * @param commands
   * @param type
   */
  private void onReceive(ReceivePack rpack,
    Collection<ReceiveCommand> commands, RepositoryHookType type)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("received git hook, type={}", type);
    }

    List<ReceiveCommand> receiveCommands = GitHooks.filterReceiveable(type,
                                             commands);

    GitFileHook.execute(type, rpack, commands);

    if (!receiveCommands.isEmpty())
    {
      handleReceiveCommands(rpack, receiveCommands, type);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("no receive commands found to process");
    }
  }

  /**
   * Resolve the name of the repository.
   * This method was introduced to fix issue #415.
   *
   * @param repository jgit repository
   *
   * @return name of repository
   *
   * @throws IOException
   */
  private String resolveRepositoryId(Repository repository)
  {
    StoredConfig gitConfig = repository.getConfig();
    return handler.getRepositoryId(gitConfig);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;

  /** Field description */
  private HookEventFacade hookEventFacade;
}
