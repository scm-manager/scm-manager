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

import com.google.inject.assistedinject.Assisted;
import org.javahg.Changeset;
import org.javahg.commands.CommitCommand;
import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Branch;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.work.WorkingCopy;
import sonia.scm.user.User;

import javax.inject.Inject;

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
