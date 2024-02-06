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


import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.spi.GitHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


public class GitReceiveHook implements PreReceiveHook, PostReceiveHook {

  private static final Logger LOG = LoggerFactory.getLogger(GitReceiveHook.class);

  private final GitRepositoryHandler handler;
  private final GitChangesetConverterFactory converterFactory;
  private final HookEventFacade hookEventFacade;

  private GitHookContextProvider postReceiveContext;

  public GitReceiveHook(GitChangesetConverterFactory converterFactory,
                        HookEventFacade hookEventFacade,
                        GitRepositoryHandler handler)
  {
    this.converterFactory = converterFactory;
    this.hookEventFacade = hookEventFacade;
    this.handler = handler;
  }

  @Override
  public void onPostReceive(ReceivePack rpack, Collection<ReceiveCommand> receiveCommands) {
    onReceive(rpack, receiveCommands, RepositoryHookType.POST_RECEIVE);
  }

  @Override
  public void onPreReceive(ReceivePack rpack, Collection<ReceiveCommand> receiveCommands) {
    onReceive(rpack, receiveCommands, RepositoryHookType.PRE_RECEIVE);
  }

  public void afterReceive() {
      if (postReceiveContext != null) {
        LOG.debug("firing {} hook for repository {}", RepositoryHookType.POST_RECEIVE, postReceiveContext.getRepositoryId());
        try {
          hookEventFacade.handle(postReceiveContext.getRepositoryId()).fireHookEvent(RepositoryHookType.POST_RECEIVE, postReceiveContext);
        } finally {
          postReceiveContext = null;
        }
      } else {
        LOG.debug("No context found for event type {}", RepositoryHookType.POST_RECEIVE);
      }
  }

  private void handleReceiveCommands(ReceivePack rpack, List<ReceiveCommand> receiveCommands, RepositoryHookType type) {
    try {
      Repository repository = rpack.getRepository();
      String repositoryId = resolveRepositoryId(repository);

      LOG.trace("resolved repository to {}", repositoryId);

      GitHookContextProvider context = new GitHookContextProvider(converterFactory, rpack, receiveCommands, repository, repositoryId);

      if (type == RepositoryHookType.POST_RECEIVE) {
        postReceiveContext = context;
      } else {
        hookEventFacade.handle(repositoryId).fireHookEvent(type, context);
      }
    } catch (Exception ex) {
      LOG.error("could not handle receive commands", ex);

      GitHooks.abortIfPossible(type, rpack, receiveCommands, ex.getMessage());
    }
  }

  private void onReceive(ReceivePack rpack, Collection<ReceiveCommand> commands, RepositoryHookType type) {
    LOG.trace("received git hook, type={}", type);

    List<ReceiveCommand> receiveCommands = GitHooks.filterReceiveable(type, commands);

    GitFileHook.execute(type, rpack, commands);

    if (!receiveCommands.isEmpty()) {
      handleReceiveCommands(rpack, receiveCommands, type);
    } else {
      LOG.debug("no receive commands found to process");
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
  private String resolveRepositoryId(Repository repository) {
    StoredConfig gitConfig = repository.getConfig();
    return handler.getRepositoryId(gitConfig);
  }
}
