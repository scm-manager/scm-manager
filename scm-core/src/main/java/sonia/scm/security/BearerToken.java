/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
