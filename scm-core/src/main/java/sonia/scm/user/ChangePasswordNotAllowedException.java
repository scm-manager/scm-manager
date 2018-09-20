package sonia.scm.user;

public class ChangePasswordNotAllowedException extends RuntimeException {

  public static final String WRON_USER_TYPE = "User of type {0} are not allowed to change password";

  public ChangePasswordNotAllowedException(String message) {
    super(message);
  }

}
