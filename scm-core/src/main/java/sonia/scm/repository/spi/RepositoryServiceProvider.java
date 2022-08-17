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

import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.CommandNotSupportedException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public abstract class RepositoryServiceProvider implements Closeable
{

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract Set<Command> getSupportedCommands();

  //~--- methods --------------------------------------------------------------

  /**
   * The default implementation of this method does nothing. If you need to
   * free resources, close connections or release locks than you have to
   * override this method.
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // should be implmentented from a service provider
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public BlameCommand getBlameCommand()
  {
    throw new CommandNotSupportedException(Command.BLAME);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public BranchesCommand getBranchesCommand()
  {
    throw new CommandNotSupportedException(Command.BRANCHES);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public BranchCommand getBranchCommand()
  {
    throw new CommandNotSupportedException(Command.BRANCH);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public BrowseCommand getBrowseCommand()
  {
    throw new CommandNotSupportedException(Command.BROWSE);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @since 1.43
   */
  public BundleCommand getBundleCommand()
  {
    throw new CommandNotSupportedException(Command.BUNDLE);
  }

  /**
   *  Method description
   *
   *
   *  @return
   */
  public CatCommand getCatCommand()
  {
    throw new CommandNotSupportedException(Command.CAT);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public DiffCommand getDiffCommand()
  {
    throw new CommandNotSupportedException(Command.DIFF);
  }

  public DiffResultCommand getDiffResultCommand()
  {
    throw new CommandNotSupportedException(Command.DIFF_RESULT);
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.31
   */
  public IncomingCommand getIncomingCommand()
  {
    throw new CommandNotSupportedException(Command.INCOMING);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public LogCommand getLogCommand()
  {
    throw new CommandNotSupportedException(Command.LOG);
  }

  /**
   * Get the corresponding {@link ModificationsCommand} implemented from the Plugins
   *
   * @return the corresponding {@link ModificationsCommand} implemented from the Plugins
   * @throws CommandNotSupportedException if there is no Implementation
   */
  public ModificationsCommand getModificationsCommand() {
    throw new CommandNotSupportedException(Command.MODIFICATIONS);
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.31
   */
  public OutgoingCommand getOutgoingCommand()
  {
    throw new CommandNotSupportedException(Command.OUTGOING);
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.31
   */
  public PullCommand getPullCommand()
  {
    throw new CommandNotSupportedException(Command.PULL);
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.31
   */
  public PushCommand getPushCommand()
  {
    throw new CommandNotSupportedException(Command.PUSH);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Feature> getSupportedFeatures()
  {
    return Collections.emptySet();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public TagsCommand getTagsCommand()
  {
    throw new CommandNotSupportedException(Command.TAGS);
  }


  /**
   * @since 2.11.0
   */
  public TagCommand getTagCommand()
  {
    throw new CommandNotSupportedException(Command.TAG);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @since 1.43
   */
  public UnbundleCommand getUnbundleCommand()
  {
    throw new CommandNotSupportedException(Command.UNBUNDLE);
  }

  /**
   * @since 2.0
   */
  public MergeCommand getMergeCommand()
  {
    throw new CommandNotSupportedException(Command.MERGE);
  }

  /**
   * @since 2.0
   */
  public ModifyCommand getModifyCommand()
  {
    throw new CommandNotSupportedException(Command.MODIFY);
  }

  /**
   * @since 2.10.0
   */
  public LookupCommand getLookupCommand()
  {
    throw new CommandNotSupportedException(Command.LOOKUP);
  }

  /**
   * @since 2.17.0
   */
  public FullHealthCheckCommand getFullHealthCheckCommand() {
    throw new CommandNotSupportedException(Command.FULL_HEALTH_CHECK);
  }

  /**
   * @since 2.19.0
   */
  public MirrorCommand getMirrorCommand() {
    throw new CommandNotSupportedException(Command.MIRROR);
  }

  /**
   * @since 2.26.0
   */
  public FileLockCommand getFileLockCommand() {
    throw new CommandNotSupportedException(Command.FILE_LOCK);
  }

  /**
   * @since 2.28.0
   */
  public BranchDetailsCommand getBranchDetailsCommand() {
    throw new CommandNotSupportedException(Command.BRANCH_DETAILS);
  }

  public ChangesetsCommand getChangesetsCommand() {
    throw new CommandNotSupportedException(Command.CHANGESETS);
  }
}
