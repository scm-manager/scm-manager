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
import jakarta.inject.Inject;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import sonia.scm.repository.Branch;
import sonia.scm.repository.BranchDetails;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.BranchDetailsCommandResult;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitBranchDetailsCommand extends AbstractGitCommand implements BranchDetailsCommand {

  @Inject
  GitBranchDetailsCommand(@Assisted GitContext context) {
    super(context);
  }

  @Override
  public BranchDetailsCommandResult execute(BranchDetailsCommandRequest branchDetailsCommandRequest) {
    String defaultBranch = context.getConfig().getDefaultBranch();
    String branchName = branchDetailsCommandRequest.getBranchName();
    if (branchName.equals(defaultBranch)) {
      return new BranchDetailsCommandResult(new BranchDetails(branchName, 0, 0));
    }
    try {
      Repository repository = open();
      ObjectId branchCommit = getObjectId(branchName, repository);
      ObjectId defaultCommit = getObjectId(defaultBranch, repository);
      return computeAheadBehind(repository, branchName, branchCommit, defaultCommit);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not compute ahead/behind", e);
    }
  }

  private ObjectId getObjectId(String branch, Repository repository) throws IOException {
    ObjectId branchCommit = getCommitOrDefault(repository, branch);
    if (branchCommit == null) {
      throw notFound(entity(Branch.class, branch).in(context.getRepository()));
    }
    return branchCommit;
  }

  private BranchDetailsCommandResult computeAheadBehind(Repository repository, String branchName, ObjectId branchCommit, ObjectId defaultCommit) throws MissingObjectException, IncorrectObjectTypeException {
    // this implementation is a copy of the implementation in org.eclipse.jgit.lib.BranchTrackingStatus
    try (RevWalk walk = new RevWalk(repository)) {

      RevCommit localCommit = walk.parseCommit(branchCommit);
      RevCommit trackingCommit = walk.parseCommit(defaultCommit);

      walk.setRevFilter(RevFilter.MERGE_BASE);
      walk.markStart(localCommit);
      walk.markStart(trackingCommit);
      RevCommit mergeBase = walk.next();

      walk.reset();
      walk.setRevFilter(RevFilter.ALL);
      int aheadCount = RevWalkUtils.count(walk, localCommit, mergeBase);
      int behindCount = RevWalkUtils.count(walk, trackingCommit, mergeBase);

      return new BranchDetailsCommandResult(new BranchDetails(branchName, aheadCount, behindCount));
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not compute ahead/behind", e);
    }
  }

  public interface Factory {
    BranchDetailsCommand create(GitContext context);
  }

}
