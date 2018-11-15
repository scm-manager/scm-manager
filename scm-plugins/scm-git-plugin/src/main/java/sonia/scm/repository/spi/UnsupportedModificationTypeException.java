package sonia.scm.repository.spi;

import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;

public class UnsupportedModificationTypeException extends InternalRepositoryException {
  public UnsupportedModificationTypeException(ContextEntry.ContextBuilder entity, String message) {
    super(entity, message);
  }
}
