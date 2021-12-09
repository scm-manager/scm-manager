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

import javax.inject.Inject;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitBranchDetailsCommand extends AbstractGitCommand implements BranchDetailsCommand {

  @Inject
  GitBranchDetailsCommand(GitContext context) {
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
}
