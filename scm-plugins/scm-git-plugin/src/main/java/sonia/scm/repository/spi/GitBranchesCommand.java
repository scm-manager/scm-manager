/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.UnknownVal;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

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

  public GitBranchesCommand(GitContext context, Repository repository)
  {
    super(context, repository);
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

  @UnknownVal
  Optional<Ref> getRepositoryHeadRef(Git git) {
    return GitUtil.getRepositoryHeadRef(git.getRepository());
  }
}
