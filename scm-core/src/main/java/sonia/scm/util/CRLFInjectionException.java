package sonia.scm.util;

public class CRLFInjectionException extends IllegalArgumentException{

  public CRLFInjectionException(String message) {
    super(message);
  }
}
