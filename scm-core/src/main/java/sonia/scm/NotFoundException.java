package sonia.scm;

public class NotFoundException extends Exception {
  public NotFoundException(String type, String id) {
    super(type + " with id '" + id + "' not found");
  }

  public NotFoundException() {
  }
}
