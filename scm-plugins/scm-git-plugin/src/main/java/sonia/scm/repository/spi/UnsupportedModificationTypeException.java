package sonia.scm.repository.spi;

import sonia.scm.repository.InternalRepositoryException;

public class UnsupportedModificationTypeException extends InternalRepositoryException {
  public UnsupportedModificationTypeException(String message) {
    super(message);
  }
}
