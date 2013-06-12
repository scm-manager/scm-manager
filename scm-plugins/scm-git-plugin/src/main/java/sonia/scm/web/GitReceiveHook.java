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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;

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
   * @param repositoryManager
   * @param handler
   */
  public GitReceiveHook(RepositoryManager repositoryManager,
    GitRepositoryHandler handler)
  {
    this.repositoryManager = repositoryManager;
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
      String repositoryName = RepositoryUtil.getRepositoryName(handler,
                                repository.getDirectory());

      repositoryManager.fireHookEvent(GitRepositoryHandler.TYPE_NAME,
        repositoryName,
        new GitRepositoryHookEvent(rpack, receiveCommands, type));
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
   * @param receiveCommands
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;

  /** Field description */
  private RepositoryManager repositoryManager;
}
