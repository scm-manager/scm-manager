package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

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
      checkOutBranch(request.getBranch());
      for (ModifyCommandRequest.PartialRequest r: request.getRequests()) {
        r.execute(this);
      }
      doCommit(request.getCommitMessage(), request.getAuthor());
      push();
      return getClone().getRepository().getRefDatabase().findRef("HEAD").getObjectId().name();
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
          throw alreadyExists(entity("file", toBeCreated).in("branch", request.getBranch()).in(context.getRepository()));
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
  }

  private String throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getRepository(), message, e);
  }
}
