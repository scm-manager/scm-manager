package sonia.scm;

public class AlreadyExistsException extends Exception {

  public AlreadyExistsException(String message) {
    super(message);
  }

  public AlreadyExistsException() {
  }
}
