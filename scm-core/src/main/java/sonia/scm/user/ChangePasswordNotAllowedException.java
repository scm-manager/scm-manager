package sonia.scm.user;

public class ChangePasswordNotAllowedException extends RuntimeException {

  public ChangePasswordNotAllowedException(String message) {
    super(message);
  }

}
