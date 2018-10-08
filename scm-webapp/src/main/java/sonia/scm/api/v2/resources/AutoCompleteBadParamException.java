package sonia.scm.api.v2.resources;

public class AutoCompleteBadParamException extends Exception {

  public static final String PARAMETER_IS_REQUIRED = "The parameter is required.";
  public static final String INVALID_PARAMETER_LENGTH = "Invalid parameter length.";

  public AutoCompleteBadParamException(String message) {
    super(message);
  }
}
