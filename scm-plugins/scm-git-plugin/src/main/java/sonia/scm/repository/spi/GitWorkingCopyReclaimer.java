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

import com.google.common.base.Stopwatch;
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

class GitWorkingCopyReclaimer {

  private static final Logger LOG = LoggerFactory.getLogger(GitWorkingCopyReclaimer.class);

  private final GitContext context;

  GitWorkingCopyReclaimer(GitContext context) {
    this.context = context;
  }

  public ParentAndClone<Repository, Repository> reclaim(File target, String initialBranch) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    LOG.trace("reclaim repository {}", context.getRepository());
    String branchToCheckout = determineBranch(initialBranch);
    Stopwatch stopwatch = Stopwatch.createStarted();
    Repository repo = openTarget(target);
    try (Git git = Git.open(target)) {
      git.reset().setMode(ResetCommand.ResetType.HARD).call();
      git.clean().setForce(true).setCleanDirectories(true).call();
      git.fetch().call();
      git.checkout().setForced(true).setName("origin/" + branchToCheckout).call();
      git.branchDelete().setBranchNames(branchToCheckout).setForce(true).call();
      git.checkout().setName(branchToCheckout).setCreateBranch(true).call();
      return new ParentAndClone<>(null, repo, target);
    } catch (GitAPIException | IOException e) {
      throw new SimpleWorkingCopyFactory.ReclaimFailedException(e);
    } finally {
      LOG.trace("took {} to reclaim repository {}", stopwatch.stop(), context.getRepository());
    }
  }

  private String determineBranch(String initialBranch) {
    if (initialBranch != null) {
      return initialBranch;
    }
    if (context.getConfig().getDefaultBranch() != null) {
      return context.getConfig().getDefaultBranch();
    }
    return context.getGlobalConfig().getDefaultBranch();
  }

  private Repository openTarget(File target) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    try {
      return GitUtil.open(target);
    } catch (IOException e) {
      throw new SimpleWorkingCopyFactory.ReclaimFailedException(e);
    }
  }
}
