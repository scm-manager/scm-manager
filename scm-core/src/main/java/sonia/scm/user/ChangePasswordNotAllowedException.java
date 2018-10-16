package sonia.scm.user;

public class ChangePasswordNotAllowedException extends RuntimeException {

  public static final String WRONG_USER_TYPE = "User of type {0} are not allowed to change password";
  @SuppressWarnings("squid:S2068")
  public static final String OLD_PASSWORD_REQUIRED = "the old password is required.";

  public ChangePasswordNotAllowedException(String message) {
    super(message);
  }

}
