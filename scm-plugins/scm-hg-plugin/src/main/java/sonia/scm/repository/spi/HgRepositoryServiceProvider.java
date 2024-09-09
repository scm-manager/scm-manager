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

import com.google.inject.Injector;
import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.CommandNotSupportedException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;


public class HgRepositoryServiceProvider extends RepositoryServiceProvider {

  public static final Set<Command> COMMANDS = EnumSet.of(
    Command.BLAME,
    Command.BROWSE,
    Command.CAT,
    Command.DIFF,
    Command.LOG,
    Command.TAGS,
    Command.TAG,
    Command.BRANCH,
    Command.BRANCHES,
    Command.INCOMING,
    Command.OUTGOING,
    Command.PUSH,
    Command.PULL,
    Command.MODIFY,
    Command.BUNDLE,
    Command.UNBUNDLE,
    Command.FULL_HEALTH_CHECK,
    Command.BRANCH_DETAILS,
    Command.CHANGESETS
  );

  public static final Set<Feature> FEATURES = EnumSet.of(
    Feature.COMBINED_DEFAULT_BRANCH,
    Feature.MODIFICATIONS_BETWEEN_REVISIONS,
    Feature.INCOMING_REVISION,
    Feature.FORCE_PUSH
  );

  private final HgCommandContext context;
   private final Injector injector;

  HgRepositoryServiceProvider(Injector injector, HgCommandContext context) {
    this.injector = injector;
    this.context = context;
  }

  @Override
  public void close() throws IOException {
    context.close();
  }

  @Override
  public HgBlameCommand getBlameCommand() {
    return injector.getInstance(HgBlameCommand.Factory.class).create(context);
  }

  @Override
  public BranchesCommand getBranchesCommand() {
    return injector.getInstance(HgBranchesCommand.Factory.class).create(context);
  }

  @Override
  public BranchCommand getBranchCommand() {
    return injector.getInstance(HgBranchCommand.Factory.class).create(context);
  }

  @Override
  public HgBrowseCommand getBrowseCommand() {
    return injector.getInstance(HgBrowseCommand.Factory.class).create(context);
  }

  @Override
  public HgCatCommand getCatCommand() {
    return injector.getInstance(HgCatCommand.Factory.class).create(context);
  }

  @Override
  public HgDiffCommand getDiffCommand() {
    return injector.getInstance(HgDiffCommand.Factory.class).create(context);
  }

  @Override
  public IncomingCommand getIncomingCommand() {
    return injector.getInstance(HgIncomingCommand.Factory.class).create(context);
  }

  @Override
  public HgLogCommand getLogCommand() {
    return injector.getInstance(HgLogCommand.Factory.class).create(context);
  }

  /**
   * Get the corresponding {@link ModificationsCommand} implemented from the Plugins
   *
   * @return the corresponding {@link ModificationsCommand} implemented from the Plugins
   * @throws CommandNotSupportedException if there is no Implementation
   */
  @Override
  public ModificationsCommand getModificationsCommand() {
    return injector.getInstance(HgModificationsCommand.Factory.class).create(context);
  }

  @Override
  public OutgoingCommand getOutgoingCommand() {
    return injector.getInstance(HgOutgoingCommand.Factory.class).create(context);
  }

  @Override
  public PullCommand getPullCommand() {
    HgLazyChangesetResolver hgLazyChangesetResolver = injector.getInstance(HgLazyChangesetResolver.Factory.class).create(context);
    HgRepositoryHookEventFactory hgRepositoryHookEventFactory = injector.getInstance(HgRepositoryHookEventFactory.Factory.class).create(context);
    return injector.getInstance(HgPullCommand.Factory.class).create(context, hgLazyChangesetResolver, hgRepositoryHookEventFactory);
  }

  @Override
  public PushCommand getPushCommand() {
    return injector.getInstance(HgPushCommand.Factory.class).create(context);
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return injector.getInstance(HgModifyCommand.Factory.class).create(context);
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
  public TagsCommand getTagsCommand() {
    return injector.getInstance(HgTagsCommand.Factory.class).create(context);
  }

  @Override
  public TagCommand getTagCommand() {
    return injector.getInstance(HgTagCommand.Factory.class).create(context);
  }

  @Override
  public BundleCommand getBundleCommand() {
    return injector.getInstance(HgBundleCommand.Factory.class).create(context);
  }

  @Override
  public UnbundleCommand getUnbundleCommand() {
    return injector.getInstance(HgUnbundleCommand.Factory.class).create(context);
  }

  @Override
  public FullHealthCheckCommand getFullHealthCheckCommand() {
    return injector.getInstance(HgFullHealthCheckCommand.Factory.class).create(context);
  }

  @Override
  public BranchDetailsCommand getBranchDetailsCommand() {
    return injector.getInstance(HgBranchDetailsCommand.Factory.class).create(context);
  }

  @Override
  public ChangesetsCommand getChangesetsCommand() {
    return injector.getInstance(HgChangesetsCommand.Factory.class).create(context);
  }
}
