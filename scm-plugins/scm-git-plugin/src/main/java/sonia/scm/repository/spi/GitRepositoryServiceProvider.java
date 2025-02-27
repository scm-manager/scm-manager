/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;

import java.util.EnumSet;
import java.util.Set;


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
    Command.CHANGESETS,
    Command.REVERT
  );

  protected static final Set<Feature> FEATURES = EnumSet.of(
    Feature.INCOMING_REVISION,
    Feature.MODIFICATIONS_BETWEEN_REVISIONS,
    Feature.FORCE_PUSH,
    Feature.TAGS_FOR_REVISION
  );

  private final Injector injector;
  private final GitContext context;


  GitRepositoryServiceProvider(Injector injector, GitContext context) {
    this.injector = injector;
    this.context = context;
  }

  @Override
  public BlameCommand getBlameCommand() {
    return injector.getInstance(GitBlameCommand.Factory.class).create(context);
  }

  @Override
  public BranchesCommand getBranchesCommand() {
    return injector.getInstance(GitBranchesCommand.Factory.class).create(context);
  }

  @Override
  public BranchCommand getBranchCommand() {
    return injector.getInstance(GitBranchCommand.Factory.class).create(context);
  }

  @Override
  public BrowseCommand getBrowseCommand() {
    return injector.getInstance(GitBrowseCommand.Factory.class).create(context);
  }

  @Override
  public CatCommand getCatCommand() {
    return injector.getInstance(GitCatCommand.Factory.class).create(context);
  }

  @Override
  public DiffCommand getDiffCommand() {
    return injector.getInstance(GitDiffCommand.Factory.class).create(context);
  }

  @Override
  public DiffResultCommand getDiffResultCommand() {
    return injector.getInstance(GitDiffResultCommand.Factory.class).create(context);
  }

  @Override
  public IncomingCommand getIncomingCommand() {
    return injector.getInstance(GitIncomingCommand.Factory.class).create(context);
  }

  @Override
  public LogCommand getLogCommand() {
    return injector.getInstance(GitLogCommand.Factory.class).create(context);
  }

  @Override
  public ModificationsCommand getModificationsCommand() {
    return injector.getInstance(GitModificationsCommand.Factory.class).create(context);
  }

  @Override
  public OutgoingCommand getOutgoingCommand() {
    return injector.getInstance(GitOutgoingCommand.Factory.class).create(context);
  }

  @Override
  public PullCommand getPullCommand() {
    PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory = injector.getInstance(PostReceiveRepositoryHookEventFactory.Factory.class).create(context);
    return injector.getInstance(GitPullCommand.Factory.class).create(context, postReceiveRepositoryHookEventFactory);
  }

  @Override
  public PushCommand getPushCommand() {
    return injector.getInstance(GitPushCommand.Factory.class).create(context);
  }

  @Override
  public TagsCommand getTagsCommand() {
    return injector.getInstance(GitTagsCommand.Factory.class).create(context);
  }

  @Override
  public TagCommand getTagCommand() {
    return injector.getInstance(GitTagCommand.Factory.class).create(context);
  }

  @Override
  public MergeCommand getMergeCommand() {
    return injector.getInstance(GitMergeCommand.Factory.class).create(context);
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return injector.getInstance(GitModifyCommand.Factory.class).create(context);
  }

  @Override
  public BundleCommand getBundleCommand() {
    return injector.getInstance(GitBundleCommand.Factory.class).create(context);
  }

  @Override
  public UnbundleCommand getUnbundleCommand() {
    return injector.getInstance(GitUnbundleCommand.Factory.class).create(context);
  }

  @Override
  public MirrorCommand getMirrorCommand() {
    return injector.getInstance(GitMirrorCommand.Factory.class).create(context);
  }

  @Override
  public FileLockCommand getFileLockCommand() {
    return injector.getInstance(GitFileLockCommand.Factory.class).create(context);
  }

  @Override
  public BranchDetailsCommand getBranchDetailsCommand() {
    return injector.getInstance(GitBranchDetailsCommand.Factory.class).create(context);
  }

  @Override
  public ChangesetsCommand getChangesetsCommand() {
    return injector.getInstance(GitChangesetsCommand.Factory.class).create(context);
  }

  @Override
  public RevertCommand getRevertCommand() {
    return injector.getInstance(GitRevertCommand.Factory.class).create(context);
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
