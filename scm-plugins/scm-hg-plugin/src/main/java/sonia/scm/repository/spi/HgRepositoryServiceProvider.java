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

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.CommandNotSupportedException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Sebastian Sdorra
 */
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
    Command.BRANCH_DETAILS
  );

  public static final Set<Feature> FEATURES = EnumSet.of(
    Feature.COMBINED_DEFAULT_BRANCH,
    Feature.MODIFICATIONS_BETWEEN_REVISIONS,
    Feature.INCOMING_REVISION
  );

  private final Injector commandInjector;
  private final HgCommandContext context;

  HgRepositoryServiceProvider(Injector injector, HgCommandContext context) {
    this.commandInjector = injector.createChildInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(HgCommandContext.class).toInstance(context);
      }
    });
    this.context = context;
  }

  @Override
  public void close() throws IOException {
    context.close();
  }

  @Override
  public HgBlameCommand getBlameCommand() {
    return new HgBlameCommand(context);
  }

  @Override
  public BranchesCommand getBranchesCommand() {
    return new HgBranchesCommand(context);
  }

  @Override
  public BranchCommand getBranchCommand() {
    return commandInjector.getInstance(HgBranchCommand.class);
  }

  @Override
  public HgBrowseCommand getBrowseCommand() {
    return new HgBrowseCommand(context);
  }

  @Override
  public HgCatCommand getCatCommand() {
    return new HgCatCommand(context);
  }

  @Override
  public HgDiffCommand getDiffCommand() {
    return new HgDiffCommand(context);
  }

  @Override
  public IncomingCommand getIncomingCommand() {
    return commandInjector.getInstance(HgIncomingCommand.class);
  }

  @Override
  public HgLogCommand getLogCommand() {
    return new HgLogCommand(context);
  }

  /**
   * Get the corresponding {@link ModificationsCommand} implemented from the Plugins
   *
   * @return the corresponding {@link ModificationsCommand} implemented from the Plugins
   * @throws CommandNotSupportedException if there is no Implementation
   */
  @Override
  public ModificationsCommand getModificationsCommand() {
    return new HgModificationsCommand(context);
  }

  @Override
  public OutgoingCommand getOutgoingCommand() {
    return commandInjector.getInstance(HgOutgoingCommand.class);
  }

  @Override
  public PullCommand getPullCommand() {
    return commandInjector.getInstance(HgPullCommand.class);
  }

  @Override
  public PushCommand getPushCommand() {
    return commandInjector.getInstance(HgPushCommand.class);
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return commandInjector.getInstance(HgModifyCommand.class);
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
    return new HgTagsCommand(context);
  }

  @Override
  public TagCommand getTagCommand() {
    return commandInjector.getInstance(HgTagCommand.class);
  }

  @Override
  public BundleCommand getBundleCommand() {
    return new HgBundleCommand(context);
  }

  @Override
  public UnbundleCommand getUnbundleCommand() {
    return commandInjector.getInstance(HgUnbundleCommand.class);
  }

  @Override
  public FullHealthCheckCommand getFullHealthCheckCommand() {
    return new HgFullHealthCheckCommand(context);
  }

  @Override
  public BranchDetailsCommand getBranchDetailsCommand() {
    return new HgBranchDetailsCommand(context);
  }
}
