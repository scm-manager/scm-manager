package sonia.scm.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateException extends RuntimeException {
  private static Logger LOG = LoggerFactory.getLogger(UpdateException.class);

  public UpdateException(String message) {
    super(message);
  }

  public UpdateException(String message, Throwable cause) {
    super(message, cause);
    LOG.error(message, cause);
  }
}
