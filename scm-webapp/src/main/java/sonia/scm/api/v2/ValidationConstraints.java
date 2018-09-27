package sonia.scm.api.v2;

public final class ValidationConstraints {

  private ValidationConstraints() {}

  /**
   * A user or group name should not start with the @ character
   * and it not contains whitespaces
   * the characters: . - _ are allowed
   */
  public static final String USER_GROUP_PATTERN = "^[^@][A-z0-9\\.\\-_]|([A-z0-9\\.\\-_]*[A-z0-9\\.\\-_])?$";

}
