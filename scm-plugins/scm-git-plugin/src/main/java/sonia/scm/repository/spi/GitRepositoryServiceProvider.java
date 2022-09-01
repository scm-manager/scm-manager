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
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Sebastian Sdorra
 */
public class GitRepositoryServiceProvider extends RepositoryServiceProvider {

  public static final Set<Command> COMMANDS = ImmutableSet.of(
    Command.BLAME,
    Command.BROWSE,
    Command.CAT,
    Command.DIFF,
    Command.DIFF_RESULT,
    Command.LOG,
    Command.TAG,
    Command.TAGS,
    Command.BRANCH,
    Command.BRANCHES,
    Command.INCOMING,
    Command.OUTGOING,
    Command.PUSH,
    Command.PULL,
    Command.MERGE,
    Command.MODIFY,
    Command.BUNDLE,
    Command.UNBUNDLE,
    Command.MIRROR,
    Command.FILE_LOCK,
    Command.BRANCH_DETAILS,
    Command.CHANGESETS
  );

  protected static final Set<Feature> FEATURES = EnumSet.of(
    Feature.INCOMING_REVISION,
    Feature.MODIFICATIONS_BETWEEN_REVISIONS
  );

  private final GitContext context;
  private final Injector commandInjector;

  //~--- constructors ---------------------------------------------------------

  GitRepositoryServiceProvider(Injector injector, GitContext context) {
    this.context = context;
    commandInjector = injector.createChildInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(GitContext.class).toInstance(context);
      }
    });
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
    return commandInjector.getInstance(GitBranchCommand.class);
  }

  @Override
  public BrowseCommand getBrowseCommand() {
    return commandInjector.getInstance(GitBrowseCommand.class);
  }

  @Override
  public CatCommand getCatCommand() {
    return commandInjector.getInstance(GitCatCommand.class);
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
    return commandInjector.getInstance(GitIncomingCommand.class);
  }

  @Override
  public LogCommand getLogCommand() {
    return commandInjector.getInstance(GitLogCommand.class);
  }

  @Override
  public ModificationsCommand getModificationsCommand() {
    return new GitModificationsCommand(context);
  }

  @Override
  public OutgoingCommand getOutgoingCommand() {
    return commandInjector.getInstance(GitOutgoingCommand.class);
  }

  @Override
  public PullCommand getPullCommand() {
    return commandInjector.getInstance(GitPullCommand.class);
  }

  @Override
  public PushCommand getPushCommand() {
    return commandInjector.getInstance(GitPushCommand.class);
  }

  @Override
  public TagsCommand getTagsCommand() {
    return commandInjector.getInstance(GitTagsCommand.class);
  }

  @Override
  public TagCommand getTagCommand() {
    return commandInjector.getInstance(GitTagCommand.class);
  }

  @Override
  public MergeCommand getMergeCommand() {
    return commandInjector.getInstance(GitMergeCommand.class);
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return commandInjector.getInstance(GitModifyCommand.class);
  }

  @Override
  public BundleCommand getBundleCommand() {
    return new GitBundleCommand(context);
  }

  @Override
  public UnbundleCommand getUnbundleCommand() {
    return commandInjector.getInstance(GitUnbundleCommand.class);
  }

  @Override
  public MirrorCommand getMirrorCommand() {
    return commandInjector.getInstance(GitMirrorCommand.class);
  }

  @Override
  public FileLockCommand getFileLockCommand() {
    return commandInjector.getInstance(GitFileLockCommand.class);
  }

  @Override
  public BranchDetailsCommand getBranchDetailsCommand() {
    return commandInjector.getInstance(GitBranchDetailsCommand.class);
  }

  @Override
  public ChangesetsCommand getChangesetsCommand() {
    return commandInjector.getInstance(GitChangesetsCommand.class);
  }

  @Override
  public Set<Command> getSupportedCommands() {
    return COMMANDS;
  }

  @Override
  public Set<Feature> getSupportedFeatures() {
    return FEATURES;
  }

  @Override
  public void close() {
    context.close();
  }
}
