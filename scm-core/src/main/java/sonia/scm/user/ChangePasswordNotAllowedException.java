package sonia.scm.user;

public class ChangePasswordNotAllowedException extends RuntimeException {

  public static final String WRONG_USER_TYPE = "User of type %s are not allowed to change password";

  public ChangePasswordNotAllowedException(String type) {
    super(String.format(WRONG_USER_TYPE, type));
  }

}
