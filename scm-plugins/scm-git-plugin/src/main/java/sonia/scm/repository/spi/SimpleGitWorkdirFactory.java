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
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.util.CacheSupportingWorkdirProvider;
import sonia.scm.repository.util.SimpleWorkdirFactory;
import sonia.scm.util.SystemUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class SimpleGitWorkdirFactory extends SimpleWorkdirFactory<Repository, Repository, GitContext> implements GitWorkdirFactory {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleGitWorkdirFactory.class);

  @Inject
  public SimpleGitWorkdirFactory(CacheSupportingWorkdirProvider workdirProvider) {
    super(workdirProvider);
  }

  @Override
  public ParentAndClone<Repository, Repository> cloneRepository(GitContext context, File target, String initialBranch) {
    LOG.trace("clone repository {}", context.getRepository().getId());
    long start = System.nanoTime();
    try {
      Repository clone = Git.cloneRepository()
        .setURI(createScmTransportProtocolUri(context.getDirectory()))
        .setDirectory(target)
        .setBranch(initialBranch)
        .call()
        .getRepository();

      Ref head = clone.exactRef(Constants.HEAD);

      if (head == null || !head.isSymbolic() || (initialBranch != null && !head.getTarget().getName().endsWith(initialBranch))) {
        throw notFound(entity("Branch", initialBranch).in(context.getRepository()));
      }

      return new ParentAndClone<>(null, clone, target);
    } catch (GitAPIException | IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone working copy of repository", e);
    } finally {
      long end = System.nanoTime();
      long duration = end - start;
      LOG.trace("took {} ns to clone repository {}", duration, context.getRepository().getId());
    }
  }

  @Override
  protected ParentAndClone<Repository, Repository> reclaimRepository(GitContext context, File target, String initialBranch) throws IOException, ReclaimFailedException {
    LOG.trace("reclaim repository {}", context.getRepository().getId());
    long start = System.nanoTime();
    Repository repo = GitUtil.open(target);
    try (Git git = Git.open(target)) {
      git.reset().setMode(ResetCommand.ResetType.HARD).call();
      git.clean().setForce(true).setCleanDirectories(true).call();
      git.fetch().call();
      git.branchDelete().setBranchNames(initialBranch).setForce(true).call();
      git.checkout().setForced(true).setName("origin/" + initialBranch).call();
      git.checkout().setName(initialBranch).setCreateBranch(true).call();
      return new ParentAndClone<>(null, repo, target);
    } catch (GitAPIException e) {
      throw new ReclaimFailedException(e);
    } finally {
      long end = System.nanoTime();
      long duration = end - start;
      LOG.trace("took {} ns to reclaim repository {}\n", duration, context.getRepository().getId());
    }
  }

  private String createScmTransportProtocolUri(File bareRepository) {
    if (SystemUtil.isWindows()) {
      return ScmTransportProtocol.NAME + ":///" + bareRepository.getAbsolutePath().replaceAll("\\\\", "/");
    } else {
      return ScmTransportProtocol.NAME + "://" + bareRepository.getAbsolutePath();
    }
  }

  @Override
  protected void closeRepository(Repository repository) {
    // we have to check for null here, because we do not create a repository for
    // the parent in cloneRepository
    if (repository != null) {
      repository.close();
    }
  }

  @Override
  protected void closeWorkdirInternal(Repository workdir) throws Exception {
    if (workdir != null) {
      workdir.close();
    }
  }

  @Override
  protected sonia.scm.repository.Repository getScmRepository(GitContext context) {
    return context.getRepository();
  }
}
