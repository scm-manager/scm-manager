package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class SimpleWorkdirFactory<T extends AutoCloseable, C> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleWorkdirFactory.class);

  private final File poolDirectory;

  private final CloneProvider<T, C> cloneProvider;

  public SimpleWorkdirFactory(CloneProvider<T, C> cloneProvider) {
    this(new File(System.getProperty("java.io.tmpdir"), "scmm-work-pool"), cloneProvider);
  }

  public SimpleWorkdirFactory(File poolDirectory, CloneProvider<T, C> cloneProvider) {
    this.poolDirectory = poolDirectory;
    this.cloneProvider = cloneProvider;
    poolDirectory.mkdirs();
  }

  public WorkingCopy<T> createWorkingCopy(C context) {
    try {
      File directory = createNewWorkdir();
      T clone = cloneProvider.cloneRepository(context, directory);
      return new WorkingCopy<>(clone, this::close, directory);
    } catch (IOException e) {
      throw new InternalRepositoryException(getRepository(context), "could not create temporary directory for clone of repository", e);
    }
  }

  protected abstract Repository getRepository(C context);

  private File createNewWorkdir() throws IOException {
    return Files.createTempDirectory(poolDirectory.toPath(),"workdir").toFile();
  }

  private void close(T repository) {
    try {
      repository.close();
    } catch (Exception e) {
      logger.warn("could not close temporary repository clone", e);
    }
  }

  public interface CloneProvider<T, C> {
    T cloneRepository(C context, File target) throws IOException;
  }
}
