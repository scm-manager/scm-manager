package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class WorkingCopy<R extends AutoCloseable> extends CloseableWrapper<R> {

  private static final Logger LOG = LoggerFactory.getLogger(WorkingCopy.class);

  private final File directory;

  public WorkingCopy(R wrappedRepository, Consumer<R> cleanup, File directory) {
    super(wrappedRepository, cleanup);
    this.directory = directory;
  }

  @Override
  public void close() {
    super.close();
    try {
      IOUtil.delete(directory);
    } catch (IOException e) {
      LOG.warn("could not delete temporary workdir '{}'", directory, e);
    }
  }
}
