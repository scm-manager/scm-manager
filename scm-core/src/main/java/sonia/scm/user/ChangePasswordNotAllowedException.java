package sonia.scm.user;

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

public class ChangePasswordNotAllowedException extends ExceptionWithContext {

  private static final String CODE = "9BR7qpDAe1";
  public static final String WRONG_USER_TYPE = "User of given type are not allowed to change password";

  public ChangePasswordNotAllowedException(ContextEntry.ContextBuilder context) {
    super(context.build(), WRONG_USER_TYPE);
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
