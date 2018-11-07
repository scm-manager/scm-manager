package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class SimpleGitWorkdirFactory implements GitWorkdirFactory {

  private static final Logger logger = LoggerFactory.getLogger(SimpleGitWorkdirFactory.class);

  private final Random random = new Random();
  private final File poolDirectory;

  public SimpleGitWorkdirFactory() {
    this(new File(System.getProperty("java.io.tmpdir"), "scmm-git-pool"));
  }

  public SimpleGitWorkdirFactory(File poolDirectory) {
    this.poolDirectory = poolDirectory;
  }

  public WorkingCopy createWorkingCopy(GitContext gitContext) {
    try {
      Repository clone = cloneRepository(gitContext.getDirectory(), new File(poolDirectory, Long.toString(random.nextLong())));
      return new WorkingCopy(clone, this::close);
    } catch (GitAPIException e) {
      throw new InternalRepositoryException("could not clone working copy of repository", e);
    }
  }

  protected Repository cloneRepository(File bareRepository, File target) throws GitAPIException {
    return Git.cloneRepository()
      .setURI(bareRepository.getAbsolutePath())
      .setDirectory(target)
      .call()
      .getRepository();
  }

  private void close(Repository repository) {
    repository.close();
    try {
      FileUtils.delete(repository.getDirectory(), FileUtils.RECURSIVE);
    } catch (IOException e) {
      logger.warn("could not delete temporary git workdir '{}'", repository.getDirectory(), e);
    }
  }
}
