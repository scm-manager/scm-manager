package sonia.scm.repository.spi;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.BadRequestException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.ContextEntry;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

public class GitModifyCommand extends AbstractGitCommand implements ModifyCommand {

  private final GitWorkdirFactory workdirFactory;

  GitModifyCommand(GitContext context, Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    return inClone(clone -> new ModifyWorker(clone, request), workdirFactory);
  }

  private class ModifyWorker extends GitCloneWorker<String> implements Worker {

    private final File workDir;
    private final ModifyCommandRequest request;

    ModifyWorker(Git clone, ModifyCommandRequest request) {
      super(clone);
      this.workDir = clone.getRepository().getWorkTree();
      this.request = request;
    }

    @Override
    String run() throws IOException {
      if (!StringUtils.isEmpty(request.getBranch())) {
        checkOutBranch(request.getBranch());
      }
      Ref head = getClone().getRepository().exactRef(Constants.HEAD);
      doThrow().violation("branch has to be a valid branch, no revision", "branch", request.getBranch()).when(head == null || !head.isSymbolic());
      getClone().getRepository().getFullBranch();
      if (!StringUtils.isEmpty(request.getExpectedRevision())) {
        if (!request.getExpectedRevision().equals(getCurrentRevision().getName())) {
          throw new ConcurrentModificationException("branch", request.getBranch() == null? "default": request.getBranch());
        }
      }
      for (ModifyCommandRequest.PartialRequest r : request.getRequests()) {
        r.execute(this);
      }
      failIfNotChanged(NoChangesMadeException::new);
      Optional<RevCommit> revCommit = doCommit(request.getCommitMessage(), request.getAuthor());
      push();
      return revCommit.orElseThrow(NoChangesMadeException::new).name();
    }

    @Override
    public void create(String toBeCreated, File file, boolean overwrite) throws IOException {
      Path targetFile = new File(workDir, toBeCreated).toPath();
      Files.createDirectories(targetFile.getParent());
      if (overwrite) {
        Files.copy(file.toPath(), targetFile, REPLACE_EXISTING);
      } else {
        try {
          Files.copy(file.toPath(), targetFile);
        } catch (FileAlreadyExistsException e) {
          ContextEntry.ContextBuilder contextBuilder = entity("file", toBeCreated);
          if (!StringUtils.isEmpty(request.getBranch())) {
            contextBuilder.in("branch", request.getBranch());
          }
          throw alreadyExists(contextBuilder.in(context.getRepository()));
        }
      }
      try {
        getClone().add().addFilepattern(toBeCreated).call();
      } catch (GitAPIException e) {
        throwInternalRepositoryException("could not add new file to index", e);
      }
    }

    @Override
    public void delete(String toBeDeleted) {

    }

    @Override
    public void modify(String path, File file) {

    }

    @Override
    public void move(String sourcePath, String targetPath) {

    }

    private class NoChangesMadeException extends BadRequestException {
      public NoChangesMadeException() {
        super(ContextEntry.ContextBuilder.entity(context.getRepository()).build(), "no changes detected to branch " + ModifyWorker.this.request.getBranch());
      }

      @Override
      public String getCode() {
        return "40RaYIeeR1";
      }
    }
  }

  private String throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getRepository(), message, e);
  }
}
