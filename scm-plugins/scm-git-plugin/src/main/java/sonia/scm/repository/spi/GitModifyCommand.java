package sonia.scm.repository.spi;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.util.WorkingCopy;
import sonia.scm.user.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitModifyCommand extends AbstractGitCommand implements ModifyCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitModifyCommand.class);

  private final GitWorkdirFactory workdirFactory;

  public GitModifyCommand(GitContext context, Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    try (WorkingCopy<org.eclipse.jgit.lib.Repository> workingCopy = workdirFactory.createWorkingCopy(context)) {
      org.eclipse.jgit.lib.Repository repository = workingCopy.getWorkingRepository();
      LOG.debug("cloned repository to folder {}", repository.getWorkTree());
      try {
        return new ModifyWorker(repository, request).execute();
      } catch (IOException e) {
        return throwInternalRepositoryException("could not apply modifications to cloned repository", e);
      }
    }
  }

  private class ModifyWorker implements Worker {

    private final Git clone;
    private final File workDir;
    private final ModifyCommandRequest request;

    ModifyWorker(org.eclipse.jgit.lib.Repository repository, ModifyCommandRequest request) {
      this.clone = new Git(repository);
      this.workDir = repository.getWorkTree();
      this.request = request;
    }

    String execute() throws IOException {
      checkOutBranch();
      for (ModifyCommandRequest.PartialRequest r: request.getRequests()) {
        r.execute(this);
      }
      return null;
    }

    @Override
    public void create(String toBeCreated, File file) throws IOException {
      Path targetFile = new File(workDir, toBeCreated).toPath();
      Files.createDirectories(targetFile.getParent());
      Files.copy(file.toPath(), targetFile);
      try {
        clone.add().addFilepattern(toBeCreated).call();
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

    private void checkOutBranch() throws IOException {
      String branch = request.getBranch();
      try {
        clone.checkout().setName(branch).call();
      } catch (RefNotFoundException e) {
        LOG.trace("could not checkout branch {} for modifications directly; trying to create local branch", branch, e);
        checkOutTargetAsNewLocalBranch();
      } catch (GitAPIException e) {
        throwInternalRepositoryException("could not checkout target branch for merge: " + branch, e);
      }
    }

    private void checkOutTargetAsNewLocalBranch() throws IOException {
      String branch = request.getBranch();
      try {
        ObjectId targetRevision = resolveRevision(branch);
        clone.checkout().setStartPoint(targetRevision.getName()).setName(branch).setCreateBranch(true).call();
      } catch (RefNotFoundException e) {
        LOG.debug("could not checkout branch {} for modifications as local branch", branch, e);
        throw notFound(entity("Branch", branch).in(context.getRepository()));
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not checkout branch for modifications as local branch: " + branch, e);
      }
    }

    private ObjectId resolveRevision(String revision) throws IOException {
      ObjectId resolved = clone.getRepository().resolve(revision);
      if (resolved == null) {
        return resolveRevisionOrThrowNotFound(clone.getRepository(), "origin/" + revision);
      } else {
        return resolved;
      }
    }

    private ObjectId resolveRevisionOrThrowNotFound(org.eclipse.jgit.lib.Repository repository, String revision) throws IOException {
      ObjectId resolved = repository.resolve(revision);
      if (resolved == null) {
        throw notFound(entity("Revision", revision).in(context.getRepository()));
      } else {
        return resolved;
      }
    }

    private void doCommit() {
      String branch = request.getBranch();
      LOG.debug("modified branch {}", branch);
      Person authorToUse = determineAuthor();
      try {
        if (!clone.status().call().isClean()) {
          clone.commit()
            .setAuthor(authorToUse.getName(), authorToUse.getMail())
            .setMessage(request.getCommitMessage())
            .call();
        }
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not commit modifications on branch " + request.getBranch(), e);
      }
    }

    private Person determineAuthor() {
      if (request.getAuthor() == null) {
        Subject subject = SecurityUtils.getSubject();
        User user = subject.getPrincipals().oneByType(User.class);
        String name = user.getDisplayName();
        String email = user.getMail();
        LOG.debug("no author set; using logged in user: {} <{}>", name, email);
        return new Person(name, email);
      } else {
        return request.getAuthor();
      }
    }
  }

  private String throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getRepository(), message, e);
  }
}
