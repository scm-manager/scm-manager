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
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.WorkingCopyFailedException;
import sonia.scm.repository.work.WorkingCopyPool;

import java.io.File;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

class GitWorkingCopyInitializer implements SimpleWorkingCopyFactory.WorkingCopyInitializer<Repository, Repository> {

  private static final Logger LOG = LoggerFactory.getLogger(GitWorkingCopyInitializer.class);

  private final SimpleGitWorkingCopyFactory simpleGitWorkingCopyFactory;
  private final GitContext context;

  public GitWorkingCopyInitializer(SimpleGitWorkingCopyFactory simpleGitWorkingCopyFactory, GitContext context) {
    this.simpleGitWorkingCopyFactory = simpleGitWorkingCopyFactory;
    this.context = context;
  }

  @Override
  public WorkingCopyPool.ParentAndClone<Repository, Repository> initialize(File target, String initialBranch) throws WorkingCopyFailedException {
    LOG.trace("clone repository {}", context.getRepository().getId());
    long start = System.nanoTime();
    try {
      Repository clone = Git.cloneRepository()
        .setURI(simpleGitWorkingCopyFactory.createScmTransportProtocolUri(context.getDirectory()))
        .setDirectory(target)
        .setBranch(initialBranch)
        .call()
        .getRepository();

      Ref head = clone.exactRef(Constants.HEAD);

      if (head == null || !head.isSymbolic() || (initialBranch != null && !head.getTarget().getName().endsWith(initialBranch))) {
        throw notFound(entity("Branch", initialBranch).in(context.getRepository()));
      }

      return new WorkingCopyPool.ParentAndClone<>(null, clone, target);
    } catch (GitAPIException | IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone working copy of repository", e);
    } finally {
      long end = System.nanoTime();
      long duration = end - start;
      LOG.trace("took {} ns to clone repository {}", duration, context.getRepository().getId());
    }
  }
}
