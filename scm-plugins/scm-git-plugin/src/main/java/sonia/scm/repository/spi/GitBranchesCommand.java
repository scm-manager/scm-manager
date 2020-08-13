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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitBranchesCommand extends AbstractGitCommand implements BranchesCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitBranchesCommand.class);

  @Inject
  public GitBranchesCommand(GitContext context)
  {
    super(context);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public List<Branch> getBranches() throws IOException {
    Git git = createGit();

    String defaultBranchName = determineDefaultBranchName(git);

    try {
      return git
        .branchList()
        .call()
        .stream()
        .map(ref -> createBranchObject(defaultBranchName, ref))
        .collect(Collectors.toList());
    } catch (GitAPIException ex) {
      throw new InternalRepositoryException(repository, "could not read branches", ex);
    }
  }

  @VisibleForTesting
  Git createGit() throws IOException {
    return new Git(open());
  }

  @Nullable
  private Branch createBranchObject(String defaultBranchName, Ref ref) {
    String branchName = GitUtil.getBranch(ref);

    if (branchName == null) {
      LOG.warn("could not determine branch name for branch name {} at revision {}", ref.getName(), ref.getObjectId());
      return null;
    } else {
      if (branchName.equals(defaultBranchName)) {
        return Branch.defaultBranch(branchName, GitUtil.getId(ref.getObjectId()));
      } else {
        return Branch.normalBranch(branchName, GitUtil.getId(ref.getObjectId()));
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

