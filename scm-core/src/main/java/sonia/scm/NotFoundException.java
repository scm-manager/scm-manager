package sonia.scm;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String type, String id) {
    super(type + " with id '" + id + "' not found");
  }

  public NotFoundException() {
  }
}
