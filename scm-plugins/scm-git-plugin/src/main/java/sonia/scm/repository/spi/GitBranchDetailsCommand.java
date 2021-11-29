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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.BranchDetailsCommandResult;

import javax.inject.Inject;
import java.io.IOException;

public class GitBranchDetailsCommand extends AbstractGitCommand implements BranchDetailsCommand {

  @Inject
  GitBranchDetailsCommand(GitContext context) {
    super(context);
  }

  @Override
  public BranchDetailsCommandResult execute(BranchDetailsCommandRequest branchDetailsCommandRequest) {
    String defaultBranch = context.getConfig().getDefaultBranch();
    if (branchDetailsCommandRequest.getBranchName().equals(defaultBranch)) {
      return new BranchDetailsCommandResult(0, 0);
    }
    try {
      Repository repository = open();
      ObjectId branchCommit = getCommitOrDefault(repository, branchDetailsCommandRequest.getBranchName());
      ObjectId defaultCommit = getCommitOrDefault(repository, defaultBranch);
      return computeAheadBehind(repository, branchCommit, defaultCommit);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not compute ahead/behind", e);
    }
  }

  private BranchDetailsCommandResult computeAheadBehind(Repository repository, ObjectId branchCommit, ObjectId defaultCommit) throws MissingObjectException, IncorrectObjectTypeException {
    try (Git git = new Git(repository)) {
      int ahead = count(git.log().addRange(defaultCommit, branchCommit).call());
      int behind = count(git.log().addRange(branchCommit, defaultCommit).call());
      return new BranchDetailsCommandResult(ahead, behind);
    } catch (GitAPIException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not compute ahead/behind", e);
    }
  }

  private int count(Iterable<RevCommit> commits) {
    Counter counter = new Counter();
    commits.forEach(c -> counter.inc());
    return counter.getCount();
  }

  private static class Counter {
    int count = 0;

    private void inc() {
      ++count;
    }

    public int getCount() {
      return count;
    }
  }
}
