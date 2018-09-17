package sonia.scm.api.v2.resources;

public class ChangePasswordNotAllowedException extends RuntimeException {

  public ChangePasswordNotAllowedException(String message) {
    super(message);
  }

}
