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

import lombok.extern.slf4j.Slf4j;
import sonia.scm.repository.Feature;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.CommandNotSupportedException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * This class is an extension base for SCM system providers to implement command functionalitites.
 * If unimplemented, the methods within this class throw {@link CommandNotSupportedException}. These are not supposed
 * to be called if unimplemented for an SCM system.
 *
 * @see sonia.scm.repository.api.RepositoryService
 * @since 1.17
 */
@Slf4j
public abstract class RepositoryServiceProvider implements Closeable {


  public abstract Set<Command> getSupportedCommands();


  /**
   * The default implementation of this method does nothing. If you need to
   * free resources, close connections or release locks than you have to
   * override this method.
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    log.warn("warning: close() has been called without implementation from a service provider.");
  }

  public Set<Feature> getSupportedFeatures() {
    return Collections.emptySet();
  }

  public BlameCommand getBlameCommand() {
    throw new CommandNotSupportedException(Command.BLAME);
  }

  public BranchesCommand getBranchesCommand() {
    throw new CommandNotSupportedException(Command.BRANCHES);
  }

  public BranchCommand getBranchCommand() {
    throw new CommandNotSupportedException(Command.BRANCH);
  }

  public BranchDetailsCommand getBranchDetailsCommand() {
    throw new CommandNotSupportedException(Command.BRANCH_DETAILS);
  }

  public BrowseCommand getBrowseCommand() {
    throw new CommandNotSupportedException(Command.BROWSE);
  }

  public BundleCommand getBundleCommand() {
    throw new CommandNotSupportedException(Command.BUNDLE);
  }

  public CatCommand getCatCommand() {
    throw new CommandNotSupportedException(Command.CAT);
  }

  public ChangesetsCommand getChangesetsCommand() {
    throw new CommandNotSupportedException(Command.CHANGESETS);
  }

  public DiffCommand getDiffCommand() {
    throw new CommandNotSupportedException(Command.DIFF);
  }

  public DiffResultCommand getDiffResultCommand() {
    throw new CommandNotSupportedException(Command.DIFF_RESULT);
  }

  public FileLockCommand getFileLockCommand() {
    throw new CommandNotSupportedException(Command.FILE_LOCK);
  }

  public FullHealthCheckCommand getFullHealthCheckCommand() {
    throw new CommandNotSupportedException(Command.FULL_HEALTH_CHECK);
  }

  public IncomingCommand getIncomingCommand() {
    throw new CommandNotSupportedException(Command.INCOMING);
  }

  public LogCommand getLogCommand() {
    throw new CommandNotSupportedException(Command.LOG);
  }

  public LookupCommand getLookupCommand() {
    throw new CommandNotSupportedException(Command.LOOKUP);
  }

  public MergeCommand getMergeCommand() {
    throw new CommandNotSupportedException(Command.MERGE);
  }

  public MirrorCommand getMirrorCommand() {
    throw new CommandNotSupportedException(Command.MIRROR);
  }

  public ModificationsCommand getModificationsCommand() {
    throw new CommandNotSupportedException(Command.MODIFICATIONS);
  }

  public ModifyCommand getModifyCommand() {
    throw new CommandNotSupportedException(Command.MODIFY);
  }

  public OutgoingCommand getOutgoingCommand() {
    throw new CommandNotSupportedException(Command.OUTGOING);
  }

  public PullCommand getPullCommand() {
    throw new CommandNotSupportedException(Command.PULL);
  }

  public PushCommand getPushCommand() {
    throw new CommandNotSupportedException(Command.PUSH);
  }

  public RevertCommand getRevertCommand() {
    throw new CommandNotSupportedException(Command.REVERT);
  }

  public TagsCommand getTagsCommand() {
    throw new CommandNotSupportedException(Command.TAGS);
  }

  public TagCommand getTagCommand() {
    throw new CommandNotSupportedException(Command.TAG);
  }

  public UnbundleCommand getUnbundleCommand() {
    throw new CommandNotSupportedException(Command.UNBUNDLE);
  }
}
