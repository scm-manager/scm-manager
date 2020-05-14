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
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.SimpleWorkingCopyFactory.ParentAndClone;

import java.io.File;
import java.io.IOException;

class GitWorkingCopyReclaimer implements SimpleWorkingCopyFactory.WorkingCopyReclaimer<Repository, Repository> {

  private static final Logger LOG = LoggerFactory.getLogger(GitWorkingCopyReclaimer.class);

  private final GitContext context;

  public GitWorkingCopyReclaimer(GitContext context) {
    this.context = context;
  }

  @Override
  public ParentAndClone<Repository, Repository> reclaim(File target, String initialBranch) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    LOG.trace("reclaim repository {}", context.getRepository().getId());
    long start = System.nanoTime();
    Repository repo = openTarget(target);
    try (Git git = Git.open(target)) {
      git.reset().setMode(ResetCommand.ResetType.HARD).call();
      git.clean().setForce(true).setCleanDirectories(true).call();
      git.fetch().call();
      git.checkout().setForced(true).setName("origin/" + initialBranch).call();
      git.branchDelete().setBranchNames(initialBranch).setForce(true).call();
      git.checkout().setName(initialBranch).setCreateBranch(true).call();
      return new ParentAndClone<>(null, repo, target);
    } catch (GitAPIException | IOException e) {
      throw new SimpleWorkingCopyFactory.ReclaimFailedException(e);
    } finally {
      long end = System.nanoTime();
      long duration = end - start;
      LOG.trace("took {} ns to reclaim repository {}\n", duration, context.getRepository().getId());
    }
  }

  private Repository openTarget(File target) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    try {
      return GitUtil.open(target);
    } catch (IOException e) {
      throw new SimpleWorkingCopyFactory.ReclaimFailedException(e);
    }
  }
}
