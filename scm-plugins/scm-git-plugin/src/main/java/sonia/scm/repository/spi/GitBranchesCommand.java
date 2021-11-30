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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static sonia.scm.repository.GitUtil.getCommit;
import static sonia.scm.repository.GitUtil.getCommitTime;

public class GitBranchesCommand extends AbstractGitCommand implements BranchesCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitBranchesCommand.class);

  @Inject
  public GitBranchesCommand(GitContext context) {
    super(context);
  }

  @Override
  public List<Branch> getBranches() throws IOException {
    Git git = createGit();

    String defaultBranchName = determineDefaultBranchName(git);

    Repository repository = git.getRepository();
    try (RevWalk refWalk = new RevWalk(repository)) {
      return git
        .branchList()
        .call()
        .stream()
        .map(ref -> createBranchObject(repository, refWalk, defaultBranchName, ref))
        .collect(Collectors.toList());
    } catch (GitAPIException ex) {
      throw new InternalRepositoryException(this.repository, "could not read branches", ex);
    }
  }

  @VisibleForTesting
  Git createGit() throws IOException {
    return new Git(open());
  }

  @Nullable
  private Branch createBranchObject(Repository repository, RevWalk refWalk, String defaultBranchName, Ref ref) {
    String branchName = GitUtil.getBranch(ref);

    if (branchName == null) {
      LOG.warn("could not determine branch name for branch name {} at revision {}", ref.getName(), ref.getObjectId());
      return null;
    } else {
      try {
        RevCommit commit = getCommit(repository, refWalk, ref);
        Long lastCommitDate = getCommitTime(commit);
        PersonIdent authorIdent = commit.getAuthorIdent();
        Person lastCommitter = new Person(authorIdent.getName(), authorIdent.getEmailAddress());
        if (branchName.equals(defaultBranchName)) {
          return Branch.defaultBranch(branchName, GitUtil.getId(ref.getObjectId()), lastCommitDate, lastCommitter);
        } else {
          return Branch.normalBranch(branchName, GitUtil.getId(ref.getObjectId()), lastCommitDate, lastCommitter);
        }
      } catch (IOException e) {
        LOG.info("failed to read commit date/author of branch {} with revision {}", branchName, ref.getName());
        return null;
      }
    }
  }

  private String determineDefaultBranchName(Git git) {
    String defaultBranchName = context.getConfig().getDefaultBranch();
    if (Strings.isNullOrEmpty(defaultBranchName)) {
      return getRepositoryHeadRef(git).map(GitUtil::getBranch).orElse(null);
    } else {
      return defaultBranchName;
    }
  }

  Optional<Ref> getRepositoryHeadRef(Git git) {
    return GitUtil.getRepositoryHeadRef(git.getRepository());
  }
}

