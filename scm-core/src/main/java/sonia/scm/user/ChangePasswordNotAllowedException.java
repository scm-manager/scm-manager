package sonia.scm.user;

public class ChangePasswordNotAllowedException extends RuntimeException {

  public static final String WRONG_USER_TYPE = "User of type {0} are not allowed to change password";
  public static final String OLD_PASSWORD_REQUIRED = "the old password is required.";

  public ChangePasswordNotAllowedException(String message) {
    super(message);
  }

}
