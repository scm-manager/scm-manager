package sonia.scm.repository.spi;

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.Repository;

public class IntegrateChangesFromWorkdirException extends ExceptionWithContext {

  private static final String CODE = "CHRM7IQzo1";

  public IntegrateChangesFromWorkdirException(Repository repository, String message) {
    super(ContextEntry.ContextBuilder.entity(repository).build(), message);
  }

  public IntegrateChangesFromWorkdirException(Repository repository, String message, Exception cause) {
    super(ContextEntry.ContextBuilder.entity(repository).build(), message, cause);
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
