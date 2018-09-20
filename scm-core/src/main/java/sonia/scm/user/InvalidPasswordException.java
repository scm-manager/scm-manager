package sonia.scm.user;

public class InvalidPasswordException extends RuntimeException {

  public static final String PASSWORD_NOT_MATCHED = "The given Password does not match with the stored one.";

  public InvalidPasswordException(String message) {
    super(message);
  }
}
