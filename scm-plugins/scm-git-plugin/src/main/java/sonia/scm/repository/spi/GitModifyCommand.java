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
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.ContextEntry;
import sonia.scm.NoChangesMadeException;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

public class GitModifyCommand extends AbstractGitCommand implements ModifyCommand {

  private static final Striped<Lock> REGISTER_LOCKS = Striped.lock(5);

  private final GitWorkingCopyFactory workingCopyFactory;
  private final LfsBlobStoreFactory lfsBlobStoreFactory;
  private final GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider;

  @Inject
  GitModifyCommand(@Assisted GitContext context, GitRepositoryHandler repositoryHandler, LfsBlobStoreFactory lfsBlobStoreFactory, GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider) {
    this(context, repositoryHandler.getWorkingCopyFactory(), lfsBlobStoreFactory, gitRepositoryConfigStoreProvider);
  }

  GitModifyCommand(@Assisted GitContext context, GitWorkingCopyFactory workingCopyFactory, LfsBlobStoreFactory lfsBlobStoreFactory, GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider) {
    super(context);
    this.workingCopyFactory = workingCopyFactory;
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.gitRepositoryConfigStoreProvider = gitRepositoryConfigStoreProvider;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    return inClone(clone -> new ModifyWorker(clone, request), workingCopyFactory, request.getBranch());
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

      boolean initialCommit = getClone().getRepository().getRefDatabase().getRefs().isEmpty();

      if (!StringUtils.isEmpty(request.getExpectedRevision())
        && !request.getExpectedRevision().equals(getCurrentObjectId().getName())) {
        throw new ConcurrentModificationException(ContextEntry.ContextBuilder.entity("Branch", request.getBranch() == null ? "default" : request.getBranch()).in(repository).build());
      }
      for (ModifyCommandRequest.PartialRequest r : request.getRequests()) {
        r.execute(this);
      }
      failIfNotChanged(() -> new NoChangesMadeException(repository, ModifyWorker.this.request.getBranch()));
      Optional<RevCommit> revCommit = doCommit(request.getCommitMessage(), request.getAuthor(), request.isSign());

      if (initialCommit) {
        handleBranchForInitialCommit();
      }

      push();
      return revCommit.orElseThrow(() -> new NoChangesMadeException(repository, ModifyWorker.this.request.getBranch())).name();
    }

    private void handleBranchForInitialCommit() {
      String branch = StringUtils.isNotBlank(request.getBranch()) ? request.getBranch() : context.getGlobalConfig().getDefaultBranch();
      if (StringUtils.isNotBlank(branch)) {
        try {
          createBranchIfNotThere(branch);
        } catch (GitAPIException | IOException e) {
          throw new InternalRepositoryException(repository, "could not create default branch for initial commit", e);
        }
      }
    }

    private void createBranchIfNotThere(String branch) throws IOException, GitAPIException {
      if (!branch.equals(getClone().getRepository().getBranch())) {
        getClone().checkout().setName(branch).setCreateBranch(true).call();
        setBranchInConfig(branch);
      }
    }

    private void setBranchInConfig(String branch) {
      gitRepositoryConfigStoreProvider.setDefaultBranch(repository, branch);
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

    @Override
    public void addMovedFileToScm(String path, Path targetPath) {
      try {
        addFileToGit(path);
      } catch (GitAPIException e) {
        throwInternalRepositoryException("could not add file to index", e);
      }
    }

    private void addFileToGit(String toBeCreated) throws GitAPIException {
      String toBeCreatedWithoutLeadingSlash = removeStartingPathSeparators(toBeCreated);
      DirCache addResult = getClone().add().addFilepattern(toBeCreatedWithoutLeadingSlash).call();
      if (addResult.findEntry(toBeCreatedWithoutLeadingSlash) < 0) {
        throw new ModificationFailedException(ContextEntry.ContextBuilder.entity("File", toBeCreated).in(repository).build(), "Could not add file to repository");
      }
    }

    @Override
    public void doScmDelete(String toBeDeleted) {
      try {
        String toBeDeletedWithoutLeadingSlash = removeStartingPathSeparators(toBeDeleted);
        DirCache deleteResult = getClone().rm().addFilepattern(toBeDeletedWithoutLeadingSlash).call();
        if (deleteResult.findEntry(toBeDeletedWithoutLeadingSlash) >= 0) {
          throw new ModificationFailedException(ContextEntry.ContextBuilder.entity("File", toBeDeleted).in(repository).build(), "Could not delete file from repository");
        }
      } catch (GitAPIException e) {
        throwInternalRepositoryException("could not remove file from index", e);
      }
    }

    @Override
    public boolean isProtectedPath(Path path) {
      return path.startsWith(getClone().getRepository().getDirectory().toPath().normalize());
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
      if (path.startsWith("/")) {
        return path.substring(1);
      }
      return path;
    }

    private String throwInternalRepositoryException(String message, Exception e) {
      throw new InternalRepositoryException(context.getRepository(), message, e);
    }
  }

  public interface Factory {
    ModifyCommand create(GitContext context);
  }

}
