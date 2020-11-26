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
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.security.GPG;

import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryServiceProvider extends RepositoryServiceProvider
{

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

  private final GitContext context;
  private final GPG gpg;
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;
  private final Injector commandInjector;

  //~--- constructors ---------------------------------------------------------

  GitRepositoryServiceProvider(Injector injector, GitContext context, GPG gpg, HookContextFactory hookContextFactory, ScmEventBus eventBus) {
    this.context = context;
    this.gpg = gpg;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
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
    return new GitTagsCommand(context, gpg);
  }

  @Override
  public TagCommand getTagCommand() {
    return new GitTagCommand(context, gpg, hookContextFactory, eventBus);
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
