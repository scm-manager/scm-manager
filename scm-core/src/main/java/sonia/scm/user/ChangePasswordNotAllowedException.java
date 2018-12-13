package sonia.scm.user;

import sonia.scm.BadRequestException;
import sonia.scm.ContextEntry;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class ChangePasswordNotAllowedException extends BadRequestException {

  private static final String CODE = "9BR7qpDAe1";
  public static final String WRONG_USER_TYPE = "Users of type %s are not allowed to change password";

  public ChangePasswordNotAllowedException(ContextEntry.ContextBuilder context, String type) {
    super(context.build(), String.format(WRONG_USER_TYPE, type));
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
