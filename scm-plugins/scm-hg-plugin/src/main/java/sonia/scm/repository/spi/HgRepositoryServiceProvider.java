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

import com.google.common.io.Closeables;
import sonia.scm.repository.Feature;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.CommandNotSupportedException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 *
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
    Command.BRANCH,
    Command.BRANCHES,
    Command.INCOMING,
    Command.OUTGOING,
    Command.PUSH,
    Command.PULL,
    Command.MODIFY
  );

  public static final Set<Feature> FEATURES = EnumSet.of(Feature.COMBINED_DEFAULT_BRANCH);

  private final HgRepositoryHandler handler;
  private final HgCommandContext context;

  HgRepositoryServiceProvider(HgRepositoryHandler handler, HgRepositoryFactory factory, Repository repository) {
    this.handler = handler;
    this.context = new HgCommandContext(handler, factory, repository);
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
    Closeables.close(context, true);
  }
  //~--- get methods ----------------------------------------------------------


  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgBlameCommand getBlameCommand()
  {
    return new HgBlameCommand(context);
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
    return new HgBranchesCommand(context);
  }

  @Override
  public BranchCommand getBranchCommand() {
    return new HgBranchCommand(context, handler.getWorkingCopyFactory());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgBrowseCommand getBrowseCommand()
  {
    return new HgBrowseCommand(context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgCatCommand getCatCommand()
  {
    return new HgCatCommand(context);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgDiffCommand getDiffCommand()
  {
    return new HgDiffCommand(context);
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
    return new HgIncomingCommand(context, handler);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgLogCommand getLogCommand()
  {
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

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public OutgoingCommand getOutgoingCommand()
  {
    return new HgOutgoingCommand(context, handler);
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
    return new HgPullCommand(handler, context);
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
    return new HgPushCommand(handler, context);
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return new HgModifyCommand(context, handler.getWorkingCopyFactory());
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
  public Set<Feature> getSupportedFeatures()
  {
    return FEATURES;
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
    return new HgTagsCommand(context);
  }

}
