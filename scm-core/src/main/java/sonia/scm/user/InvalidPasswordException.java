package sonia.scm.user;

public class InvalidPasswordException extends RuntimeException {

  public InvalidPasswordException() {
    super("The given Password does not match with the stored one.");
  }
}
