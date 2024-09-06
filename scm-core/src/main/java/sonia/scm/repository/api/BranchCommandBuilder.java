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

package sonia.scm.repository.api;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Branch;
import sonia.scm.repository.BranchCreatedEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.BranchCommand;

/**
 * @since 2.0
 */
public final class BranchCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(BranchCommandBuilder.class);

  private final Repository repository;
  private final BranchCommand command;
  private final ScmEventBus eventBus;
  private final BranchRequest request = new BranchRequest();

  public BranchCommandBuilder(Repository repository, BranchCommand command) {
    this(repository, command, ScmEventBus.getInstance());
  }

  /**
   * Creates a new {@link BranchCommandBuilder}.
   *
   * @param command type specific command implementation
   *
   * @deprecated use {@link #BranchCommandBuilder(Repository, BranchCommand)} instead.
   */
  @Deprecated
  public BranchCommandBuilder(BranchCommand command) {
    this(null, command, ScmEventBus.getInstance());
  }

  @VisibleForTesting
  BranchCommandBuilder(Repository repository, BranchCommand command, ScmEventBus eventBus) {
    this.repository = repository;
    this.command = command;
    this.eventBus = eventBus;
  }

  /**
   * Specifies the source branch, which the new branch should be based on.
   *
   * @param parentBranch The base branch for the new branch.
   * @return This builder.
   */
  public BranchCommandBuilder from(String parentBranch) {
    request.setParentBranch(parentBranch);
    return this;
  }

  /**
   * Execute the command and create a new branch with the given name.
   * @param name The name of the new branch.
   * @return The created branch.
   */
  public Branch branch(String name) {
    request.setNewBranch(name);
    Branch branch = command.branch(request);
    fireCreatedEvent(branch);
    return branch;
  }

  private void fireCreatedEvent(Branch branch) {
    if (repository != null) {
      eventBus.post(new BranchCreatedEvent(repository, branch.getName()));
    } else {
      LOG.warn("the branch command was created without a repository, so we are not able to fire a BranchCreatedEvent");
    }
  }

  public void delete(String branchName) {
    command.deleteOrClose(branchName);
  }
}
