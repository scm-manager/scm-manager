package sonia.scm.repository;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.util.Random;


/**
 * Config:
 *
 * 1. Overall and absolute maximum of temp work directories
 * 2. Maximum number of temp work directories pooled overall
 * 3. Maximum number of temp work directories pooled for one master repository
 */
public class GitWorkdirPool {

  private final Random random = new Random();
  private final File poolDirectory;

  public GitWorkdirPool() {
    this(new File(System.getProperty("java.io.tmpdir")));
  }

  public GitWorkdirPool(File poolDirectory) {
    this.poolDirectory = poolDirectory;
  }

  public CloseableWrapper<Repository> getWorkingCopy(File bareRepository) {
    try {
      Git clone = cloneRepository(bareRepository, new File(poolDirectory, Long.toString(random.nextLong())));
      return new CloseableWrapper<>(clone.getRepository(), r -> clone.close());
    } catch (GitAPIException e) {
      throw new InternalRepositoryException("could not clone working copy of repository", e);
    }
  }

  protected Git cloneRepository(File bareRepository, File target) throws GitAPIException {
    return Git.cloneRepository()
      .setURI(bareRepository.getAbsolutePath())
      .setDirectory(target)
      .call();
  }
}
