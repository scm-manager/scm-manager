package sonia.scm.repository.spi;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class GitModifyCommand extends AbstractGitCommand implements ModifyCommand {

  private final GitWorkdirFactory workdirFactory;

  GitModifyCommand(GitContext context, Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    return inClone(clone -> new ModifyWorker(clone, request), workdirFactory, request.getBranch());
  }

  private class ModifyWorker extends GitCloneWorker<String> implements ModifyWorkerHelper {

    private final File workDir;
    private final ModifyCommandRequest request;

    ModifyWorker(Git clone, ModifyCommandRequest request) {
      super(clone);
      this.workDir = clone.getRepository().getWorkTree();
      this.request = request;
    }

    @Override
    String run() throws IOException {
      getClone().getRepository().getFullBranch();
      if (!StringUtils.isEmpty(request.getExpectedRevision())) {
        if (!request.getExpectedRevision().equals(getCurrentRevision().getName())) {
          throw new ConcurrentModificationException("branch", request.getBranch() == null? "default": request.getBranch());
        }
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
      try {
        addFileToGit(name);
      } catch (GitAPIException e) {
        throwInternalRepositoryException("could not add new file to index", e);
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
  }

  private String throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getRepository(), message, e);
  }
}
