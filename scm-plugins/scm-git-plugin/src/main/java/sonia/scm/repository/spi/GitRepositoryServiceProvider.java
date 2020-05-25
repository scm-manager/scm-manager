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

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryServiceProvider extends RepositoryServiceProvider
{

  /** Field description */
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

  //~--- constructors ---------------------------------------------------------

  public GitRepositoryServiceProvider(GitRepositoryHandler handler, Repository repository, GitRepositoryConfigStoreProvider storeProvider, LfsBlobStoreFactory lfsBlobStoreFactory, HookContextFactory hookContextFactory, ScmEventBus eventBus, SyncAsyncExecutorProvider executorProvider) {
    this.handler = handler;
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.executorProvider = executorProvider;
    this.context = new GitContext(handler.getDirectory(repository.getId()), repository, storeProvider);
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
    context.close();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public BlameCommand getBlameCommand()
  {
    return new GitBlameCommand(context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public BranchesCommand getBranchesCommand()
  {
    return new GitBranchesCommand(context);
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
    return new GitBranchCommand(context, hookContextFactory, eventBus);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public BrowseCommand getBrowseCommand()
  {
    return new GitBrowseCommand(context, lfsBlobStoreFactory, executorProvider.createExecutorWithDefaultTimeout());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public CatCommand getCatCommand()
  {
    return new GitCatCommand(context, lfsBlobStoreFactory);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public DiffCommand getDiffCommand()
  {
    return new GitDiffCommand(context);
  }

  @Override
  public DiffResultCommand getDiffResultCommand() {
    return new GitDiffResultCommand(context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public IncomingCommand getIncomingCommand()
  {
    return new GitIncomingCommand(handler, context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public LogCommand getLogCommand()
  {
    return new GitLogCommand(context);
  }

  @Override
  public ModificationsCommand getModificationsCommand() {
    return new GitModificationsCommand(context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public OutgoingCommand getOutgoingCommand()
  {
    return new GitOutgoingCommand(handler, context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public PullCommand getPullCommand()
  {
    return new GitPullCommand(handler, context);
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
    return new GitPushCommand(handler, context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<Command> getSupportedCommands()
  {
    return COMMANDS;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public TagsCommand getTagsCommand()
  {
    return new GitTagsCommand(context);
  }

  @Override
  public MergeCommand getMergeCommand() {
    return new GitMergeCommand(context, handler.getWorkdirFactory());
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return new GitModifyCommand(context, handler.getWorkdirFactory(), lfsBlobStoreFactory);
  }

  @Override
  public Set<Feature> getSupportedFeatures() {
    return FEATURES;
  }
//~--- fields ---------------------------------------------------------------

  /** Field description */
  private final GitContext context;

  /** Field description */
  private final GitRepositoryHandler handler;

  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  private final HookContextFactory hookContextFactory;

  private final ScmEventBus eventBus;

  private final SyncAsyncExecutorProvider executorProvider;
}
