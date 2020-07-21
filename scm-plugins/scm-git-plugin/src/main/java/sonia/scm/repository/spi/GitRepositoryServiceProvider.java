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

package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableSet;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.cache.CacheManager;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Feature;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class GitRepositoryServiceProvider extends RepositoryServiceProvider {

  private final GitContext context;
  private final GitRepositoryHandler handler;
  private final LfsBlobStoreFactory lfsBlobStoreFactory;
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;
  private final SyncAsyncExecutorProvider executorProvider;
  private final CacheManager cacheManager;

  //J-
  public static final Set<Command> COMMANDS = ImmutableSet.of(
    Command.BLAME,
    Command.BROWSE,
    Command.CAT,
    Command.DIFF,
    Command.DIFF_RESULT,
    Command.LOG,
    Command.TAGS,
    Command.BRANCH,
    Command.BRANCHES,
    Command.INCOMING,
    Command.OUTGOING,
    Command.PUSH,
    Command.PULL,
    Command.MERGE,
    Command.MODIFY
  );
  protected static final Set<Feature> FEATURES = EnumSet.of(Feature.INCOMING_REVISION);
  //J+

  public GitRepositoryServiceProvider(GitRepositoryHandler handler, Repository repository, GitRepositoryConfigStoreProvider storeProvider, LfsBlobStoreFactory lfsBlobStoreFactory, HookContextFactory hookContextFactory, ScmEventBus eventBus, SyncAsyncExecutorProvider executorProvider, CacheManager cacheManager) {
    this.handler = handler;
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.executorProvider = executorProvider;
    this.cacheManager = cacheManager;
    this.context = new GitContext(handler.getDirectory(repository.getId()), repository, storeProvider);
  }

  @Override
  public void close() throws IOException {
    context.close();
  }

  @Override
  public BlameCommand getBlameCommand() {
    return new GitBlameCommand(context);
  }

  @Override
  public BranchesCommand getBranchesCommand() {
    return new GitBranchesCommand(context);
  }

  @Override
  public BranchCommand getBranchCommand() {
    return new GitBranchCommand(context, hookContextFactory, eventBus, cacheManager);
  }

  @Override
  public BrowseCommand getBrowseCommand() {
    return new GitBrowseCommand(context, lfsBlobStoreFactory, executorProvider.createExecutorWithDefaultTimeout());
  }

  @Override
  public CatCommand getCatCommand() {
    return new GitCatCommand(context, lfsBlobStoreFactory);
  }

  @Override
  public DiffCommand getDiffCommand() {
    return new GitDiffCommand(context);
  }

  @Override
  public DiffResultCommand getDiffResultCommand() {
    return new GitDiffResultCommand(context);
  }

  @Override
  public IncomingCommand getIncomingCommand() {
    return new GitIncomingCommand(handler, context);
  }

  @Override
  public LogCommand getLogCommand() {
    return new GitLogCommand(context);
  }

  @Override
  public ModificationsCommand getModificationsCommand() {
    return new GitModificationsCommand(context);
  }

  @Override
  public OutgoingCommand getOutgoingCommand() {
    return new GitOutgoingCommand(handler, context);
  }

  @Override
  public PullCommand getPullCommand() {
    return new GitPullCommand(handler, context);
  }

  @Override
  public PushCommand getPushCommand() {
    return new GitPushCommand(handler, context);
  }

  @Override
  public Set<Command> getSupportedCommands() {
    return COMMANDS;
  }

  @Override
  public TagsCommand getTagsCommand() {
    return new GitTagsCommand(context);
  }

  @Override
  public MergeCommand getMergeCommand() {
    return new GitMergeCommand(context, handler.getWorkingCopyFactory());
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return new GitModifyCommand(context, handler.getWorkingCopyFactory(), lfsBlobStoreFactory);
  }

  @Override
  public Set<Feature> getSupportedFeatures() {
    return FEATURES;
  }
}
