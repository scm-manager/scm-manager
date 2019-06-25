package sonia.scm.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventor;

/**
 * Logging adapter for {@link ClassLoaderLeakPreventor}.
 */
public class LoggingAdapter implements se.jiderhamn.classloader.leak.prevention.Logger {

  @SuppressWarnings("squid:S3416") // suppress "loggers should be named for their enclosing classes" rule
  private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderLeakPreventor.class);

  @Override
  public void debug(String msg) {
    LOG.debug(msg);
  }

  @Override
  public void info(String msg) {
    LOG.info(msg);
  }

  @Override
  public void warn(String msg) {
    LOG.warn(msg);
  }

  @Override
  public void warn(Throwable t) {
    LOG.warn(t.getMessage(), t);
  }

  @Override
  public void error(String msg) {
    LOG.error(msg);
  }

  @Override
  public void error(Throwable t) {
    LOG.error(t.getMessage(), t);
  }
}
