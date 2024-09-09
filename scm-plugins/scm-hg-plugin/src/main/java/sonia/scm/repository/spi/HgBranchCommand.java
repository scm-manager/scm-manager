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

import com.google.inject.assistedinject.Assisted;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.javahg.Changeset;
import org.javahg.commands.CommitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Branch;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.work.WorkingCopy;
import sonia.scm.user.User;

/**
 * Mercurial implementation of the {@link BranchCommand}.
 * Note that this creates an empty commit to "persist" the new branch.
 */
public class HgBranchCommand extends AbstractWorkingCopyCommand implements BranchCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgBranchCommand.class);

  @Inject
  HgBranchCommand(@Assisted HgCommandContext context, HgRepositoryHandler handler) {
    this(context, handler.getWorkingCopyFactory());
  }

  @VisibleForTesting
  HgBranchCommand(HgCommandContext context, HgWorkingCopyFactory workingCopyFactory) {
    super(context, workingCopyFactory);
  }

  @Override
  public Branch branch(BranchRequest request) {
    try (WorkingCopy<org.javahg.Repository, org.javahg.Repository> workingCopy = workingCopyFactory.createWorkingCopy(getContext(), request.getParentBranch())) {
      org.javahg.Repository repository = workingCopy.getWorkingRepository();

      Changeset emptyChangeset = createNewBranchWithEmptyCommit(request, repository);

      LOG.debug("Created new branch '{}' in repository {} with changeset {}",
        request.getNewBranch(), getRepository().getNamespaceAndName(), emptyChangeset.getNode());

      pullChangesIntoCentralRepository(workingCopy, request.getNewBranch());

      return Branch.normalBranch(request.getNewBranch(), emptyChangeset.getNode());
    }
  }

  @Override
  public void deleteOrClose(String branchName) {
    try (WorkingCopy<org.javahg.Repository, org.javahg.Repository> workingCopy = workingCopyFactory.createWorkingCopy(getContext(), branchName)) {
      User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);

      LOG.debug("Closing branch '{}' in repository {}", branchName, getRepository());

      org.javahg.commands.CommitCommand
        .on(workingCopy.getWorkingRepository())
        .user(getFormattedUser(currentUser))
        .message(String.format("Close branch: %s", branchName))
        .closeBranch()
        .execute();
      pullChangesIntoCentralRepository(workingCopy, branchName);
    } catch (Exception ex) {
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity(getContext().getScmRepository()), String.format("Could not close branch: %s", branchName));
    }
  }

  private String getFormattedUser(User currentUser) {
    return String.format("%s <%s>", currentUser.getDisplayName(), currentUser.getMail());
  }

  private Changeset createNewBranchWithEmptyCommit(BranchRequest request, org.javahg.Repository repository) {
    org.javahg.commands.BranchCommand.on(repository).set(request.getNewBranch());
    User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    return CommitCommand
      .on(repository)
      .user(getFormattedUser(currentUser))
      .message("Create new branch " + request.getNewBranch())
      .execute();
  }

  public interface Factory {
    HgBranchCommand create(HgCommandContext context);
  }
}
