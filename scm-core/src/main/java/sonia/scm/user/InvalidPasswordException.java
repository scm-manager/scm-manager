package sonia.scm.user;

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

public class InvalidPasswordException extends ExceptionWithContext {

  private static final String CODE = "8YR7aawFW1";

  public InvalidPasswordException(ContextEntry.ContextBuilder passwordChange) {
    super(passwordChange.build(), "The given old password does not match with the stored one.");
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
