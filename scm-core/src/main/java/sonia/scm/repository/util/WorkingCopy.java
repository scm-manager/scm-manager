package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class WorkingCopy<R> implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(WorkingCopy.class);

  private final File directory;
  private final R workingRepository;
  private final R centralRepository;
  private final Consumer<R> cleanup;

  public WorkingCopy(R workingRepository, R centralRepository, Consumer<R> cleanup, File directory) {
    this.directory = directory;
    this.workingRepository = workingRepository;
    this.centralRepository = centralRepository;
    this.cleanup = cleanup;
  }

  public R getWorkingRepository() {
    return workingRepository;
  }

  public R getCentralRepository() {
    return centralRepository;
  }

  public File getDirectory() {
    return directory;
  }

  @Override
  public void close() {
    try {
      cleanup.accept(workingRepository);
      cleanup.accept(centralRepository);
      IOUtil.delete(directory);
    } catch (IOException e) {
      LOG.warn("could not delete temporary workdir '{}'", directory, e);
    }
  }
}
