package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class WorkingCopy<R, W> implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(WorkingCopy.class);

  private final File directory;
  private final W workingRepository;
  private final R centralRepository;
  private final Consumer<W> cleanupWorkdir;
  private final Consumer<R> cleanupCentral;

  public WorkingCopy(W workingRepository, R centralRepository, Consumer<W> cleanupWorkdir, Consumer<R> cleanupCentral, File directory) {
    this.directory = directory;
    this.workingRepository = workingRepository;
    this.centralRepository = centralRepository;
    this.cleanupCentral = cleanupCentral;
    this.cleanupWorkdir = cleanupWorkdir;
  }

  public W getWorkingRepository() {
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
      cleanupWorkdir.accept(workingRepository);
      cleanupCentral.accept(centralRepository);
      IOUtil.delete(directory);
    } catch (IOException e) {
      LOG.warn("could not delete temporary workdir '{}'", directory, e);
    }
  }
}
