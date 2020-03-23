package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import sonia.scm.util.HttpUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Optional;

/**
 * Client side session id.
 */
@EqualsAndHashCode
public final class SessionId implements Serializable {

  @VisibleForTesting
  public static final String PARAMETER = "X-SCM-Session-ID";

  private final String value;

  private SessionId(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Optional<SessionId> from(HttpServletRequest request) {
    return HttpUtil.getHeaderOrGetParameter(request, PARAMETER).map(SessionId::valueOf);
  }

  public static SessionId valueOf(String value) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(value), "session id could not be empty or null");
    return new SessionId(value);
  }
}
