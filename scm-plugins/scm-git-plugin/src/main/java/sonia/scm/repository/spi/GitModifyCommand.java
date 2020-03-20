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

import com.google.common.util.concurrent.Striped;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

public class GitModifyCommand extends AbstractGitCommand implements ModifyCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitModifyCommand.class);
  private static final Striped<Lock> REGISTER_LOCKS = Striped.lock(5);

  private final GitWorkdirFactory workdirFactory;
  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  GitModifyCommand(GitContext context, Repository repository, GitWorkdirFactory workdirFactory, LfsBlobStoreFactory lfsBlobStoreFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    return inClone(clone -> new ModifyWorker(clone, request), workdirFactory, request.getBranch());
  }

  private class ModifyWorker extends GitCloneWorker<String> implements ModifyWorkerHelper {

    private final File workDir;
    private final ModifyCommandRequest request;

    ModifyWorker(Git clone, ModifyCommandRequest request) {
      super(clone, context, repository);
      this.workDir = clone.getRepository().getWorkTree();
      this.request = request;
    }

    @Override
    String run() throws IOException {
      getClone().getRepository().getFullBranch();
      if (!StringUtils.isEmpty(request.getExpectedRevision())
        && !request.getExpectedRevision().equals(getCurrentRevision().getName())) {
        throw new ConcurrentModificationException("branch", request.getBranch() == null ? "default" : request.getBranch());
      }
      for (ModifyCommandRequest.PartialRequest r : request.getRequests()) {
        r.execute(this);
      }
      failIfNotChanged(() -> new NoChangesMadeException(repository, ModifyWorker.this.request.getBranch()));
      Optional<RevCommit> revCommit = doCommit(request.getCommitMessage(), request.getAuthor());
      push();
      return revCommit.orElseThrow(() -> new NoChangesMadeException(repository, ModifyWorker.this.request.getBranch())).name();
    }

    @Override
    public void addFileToScm(String name, Path file) {
      addToGitWithLfsSupport(name, file);
    }

    private void addToGitWithLfsSupport(String path, Path targetFile) {
      REGISTER_LOCKS.get(targetFile).lock();
      try {
        LfsBlobStoreCleanFilterFactory cleanFilterFactory = new LfsBlobStoreCleanFilterFactory(lfsBlobStoreFactory, repository, targetFile);

        String registerKey = "git-lfs clean -- '" + path + "'";
        LOG.debug("register lfs filter command factory for command '{}'", registerKey);
        FilterCommandRegistry.register(registerKey, cleanFilterFactory::createFilter);
        try {
          addFileToGit(path);
        } catch (GitAPIException e) {
          throwInternalRepositoryException("could not add file to index", e);
        } finally {
          LOG.debug("unregister lfs filter command factory for command \"{}\"", registerKey);
          FilterCommandRegistry.unregister(registerKey);
        }
      } finally {
        REGISTER_LOCKS.get(targetFile).unlock();
      }
    }

    private void addFileToGit(String toBeCreated) throws GitAPIException {
      getClone().add().addFilepattern(removeStartingPathSeparators(toBeCreated)).call();
    }

    @Override
    public void doScmDelete(String toBeDeleted) {
      try {
        getClone().rm().addFilepattern(removeStartingPathSeparators(toBeDeleted)).call();
      } catch (GitAPIException e) {
        throwInternalRepositoryException("could not remove file from index", e);
      }
    }

    @Override
    public File getWorkDir() {
      return workDir;
    }

    @Override
    public Repository getRepository() {
      return repository;
    }

    @Override
    public String getBranch() {
      return request.getBranch();
    }

    private String removeStartingPathSeparators(String path) {
      while (path.startsWith(File.separator)) {
        path = path.substring(1);
      }
      return path;
    }

    private String throwInternalRepositoryException(String message, Exception e) {
      throw new InternalRepositoryException(context.getRepository(), message, e);
    }
  }
}
