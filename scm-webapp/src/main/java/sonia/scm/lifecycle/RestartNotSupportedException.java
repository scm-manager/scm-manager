package sonia.scm.lifecycle;

/**
 * Exception is thrown if a restart is not supported or a restart strategy is misconfigured.
 */
public class RestartNotSupportedException extends RuntimeException {
  RestartNotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }
}
