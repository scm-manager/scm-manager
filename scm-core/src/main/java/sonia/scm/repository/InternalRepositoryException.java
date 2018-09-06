package sonia.scm.repository;

public class InternalRepositoryException extends RuntimeException {
  public InternalRepositoryException(Throwable ex) {
    super(ex);
  }

  public InternalRepositoryException(String msg, Exception ex) {
    super(msg, ex);
  }

  public InternalRepositoryException(String message) {
    super(message);
  }
}
