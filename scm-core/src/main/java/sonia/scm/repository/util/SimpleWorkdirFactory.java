package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class SimpleWorkdirFactory<R, C> implements WorkdirFactory<R, C> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleWorkdirFactory.class);

  private final File poolDirectory;

  public SimpleWorkdirFactory() {
    this(new File(System.getProperty("java.io.tmpdir"), "scmm-work-pool"));
  }

  public SimpleWorkdirFactory(File poolDirectory) {
    this.poolDirectory = poolDirectory;
    if (!poolDirectory.exists() && !poolDirectory.mkdirs()) {
      throw new IllegalStateException("could not create pool directory " + poolDirectory);
    }
  }

  @Override
  public WorkingCopy<R> createWorkingCopy(C context) {
    try {
      File directory = createNewWorkdir();
      ParentAndClone<R> parentAndClone = cloneRepository(context, directory);
      return new WorkingCopy<>(parentAndClone.getClone(), parentAndClone.getParent(), this::close, directory);
    } catch (IOException e) {
      throw new InternalRepositoryException(getScmRepository(context), "could not clone repository in temporary directory", e);
    }
  }

  protected abstract Repository getScmRepository(C context);

  @SuppressWarnings("squid:S00112")
  // We do allow implementations to throw arbitrary exceptions here, so that we can handle them in close
  protected abstract void closeRepository(R repository) throws Exception;

  protected abstract ParentAndClone<R> cloneRepository(C context, File target) throws IOException;

  private File createNewWorkdir() throws IOException {
    return Files.createTempDirectory(poolDirectory.toPath(),"workdir").toFile();
  }

  private void close(R repository) {
    try {
      closeRepository(repository);
    } catch (Exception e) {
      logger.warn("could not close temporary repository clone", e);
    }
  }

  protected static class ParentAndClone<R> {
    private final R parent;
    private final R clone;

    public ParentAndClone(R parent, R clone) {
      this.parent = parent;
      this.clone = clone;
    }

    public R getParent() {
      return parent;
    }

    public R getClone() {
      return clone;
    }
  }
}
