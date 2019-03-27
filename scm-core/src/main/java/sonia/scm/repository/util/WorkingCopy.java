package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class WorkingCopy<T> extends CloseableWrapper<T> {

  private static final Logger LOG = LoggerFactory.getLogger(WorkingCopy.class);

  private final File directory;

  public WorkingCopy(T wrapped, Consumer<T> cleanup, File directory) {
    super(wrapped, cleanup);
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
