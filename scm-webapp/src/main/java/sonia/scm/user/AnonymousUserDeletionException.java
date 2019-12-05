package sonia.scm.user;

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

public class AnonymousUserDeletionException extends ExceptionWithContext {

  private static final String CODE = "1yRiASshD1";

  public AnonymousUserDeletionException(ContextEntry.ContextBuilder context) {
    super(context.build(), "_anonymous user can not be deleted if anonymous access is enabled");
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
