/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.security;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import jakarta.annotation.Nullable;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * Token used for authentication with bearer tokens.
 *
 * @since 2.0.0
 */
public final class BearerToken implements AuthenticationToken {

  private final SessionId sessionId;
  private final String raw;

  /**
   * Constructs a new instance.
   *
   * @param sessionId session id of the client
   * @param raw raw bearer token
   */
  private BearerToken(SessionId sessionId, String raw) {
    this.sessionId = sessionId;
    this.raw = raw;
  }

  /**
   * Returns the wrapped raw format of the token.
   */
  @Override
  public String getCredentials() {
    return raw;
  }

  /**
   * Returns the session id or {@code null}.
   */
  @Override
  public SessionId getPrincipal() {
    return sessionId;
  }

  /**
   * Creates a new {@link BearerToken} from raw string representation.
   *
   * @param raw string representation
   */
  public static BearerToken valueOf(String raw){
    Preconditions.checkArgument(!Strings.isNullOrEmpty(raw), "raw token is required");
    return new BearerToken(null, raw);
  }

  /**
   * Creates a new {@link BearerToken} from raw string representation for the given ui session id.
   *
   * @param session session id of the client
   * @param rawToken bearer token string representation
   */
  public static BearerToken create(@Nullable SessionId session, String rawToken) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(rawToken), "raw token is required");
    return new BearerToken(session, rawToken);
  }
}
