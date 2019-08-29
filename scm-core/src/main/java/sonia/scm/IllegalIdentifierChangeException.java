package sonia.scm;

import java.util.Collections;

public class IllegalIdentifierChangeException extends BadRequestException {

  private static final String CODE = "thbsUFokjk";

  public IllegalIdentifierChangeException(ContextEntry.ContextBuilder context, String message) {
    super(context.build(), message);
  }

  public IllegalIdentifierChangeException(String message) {
    super(Collections.emptyList(), message);
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
