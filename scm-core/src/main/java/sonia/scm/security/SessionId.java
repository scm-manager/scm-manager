package sonia.scm.security;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Objects;

/**
 * Client side session id.
 */
public final class SessionId {

  private final String value;

  private SessionId(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SessionId sessionID = (SessionId) o;
    return Objects.equals(value, sessionID.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }

  public static SessionId valueOf(String value) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(value), "session id could not be empty or null");
    return new SessionId(value);
  }
}
