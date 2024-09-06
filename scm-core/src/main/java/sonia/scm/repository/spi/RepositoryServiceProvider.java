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

import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.CommandNotSupportedException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @since 1.17
 */
public abstract class RepositoryServiceProvider implements Closeable
{

  
  public abstract Set<Command> getSupportedCommands();


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


  
  public BlameCommand getBlameCommand()
  {
    throw new CommandNotSupportedException(Command.BLAME);
  }

  
  public BranchesCommand getBranchesCommand()
  {
    throw new CommandNotSupportedException(Command.BRANCHES);
  }

  
  public BranchCommand getBranchCommand()
  {
    throw new CommandNotSupportedException(Command.BRANCH);
  }

  
  public BrowseCommand getBrowseCommand()
  {
    throw new CommandNotSupportedException(Command.BROWSE);
  }

  /**
   * @since 1.43
   */
  public BundleCommand getBundleCommand()
  {
    throw new CommandNotSupportedException(Command.BUNDLE);
  }

  public CatCommand getCatCommand()
  {
    throw new CommandNotSupportedException(Command.CAT);
  }

  
  public DiffCommand getDiffCommand()
  {
    throw new CommandNotSupportedException(Command.DIFF);
  }

  public DiffResultCommand getDiffResultCommand()
  {
    throw new CommandNotSupportedException(Command.DIFF_RESULT);
  }

  /**
   * @since 1.31
   */
  public IncomingCommand getIncomingCommand()
  {
    throw new CommandNotSupportedException(Command.INCOMING);
  }

  
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
   * @since 1.31
   */
  public OutgoingCommand getOutgoingCommand()
  {
    throw new CommandNotSupportedException(Command.OUTGOING);
  }

  /**
   * @since 1.31
   */
  public PullCommand getPullCommand()
  {
    throw new CommandNotSupportedException(Command.PULL);
  }

  /**
   * @since 1.31
   */
  public PushCommand getPushCommand()
  {
    throw new CommandNotSupportedException(Command.PUSH);
  }

  
  public Set<Feature> getSupportedFeatures()
  {
    return Collections.emptySet();
  }

  
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
