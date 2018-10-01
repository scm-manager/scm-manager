package sonia.scm.api.v2;

public final class ValidationConstraints {

  private ValidationConstraints() {}

  /**
   * A user or group name should not start with <code>@</code> or a whitespace
   * and it not contains whitespaces
   * and the characters: . - _ @ are allowed
   */
  public static final String USER_GROUP_PATTERN = "^[A-Za-z0-9\\.\\-_][A-Za-z0-9\\.\\-_@]+$";

}
