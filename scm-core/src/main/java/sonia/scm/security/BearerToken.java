/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationToken;

import javax.annotation.Nullable;

/**
 * Token used for authentication with bearer tokens.
 *
 * @author Sebastian Sdorra
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
   *
   * @return raw format
   */
  @Override
  public String getCredentials() {
    return raw;
  }

  /**
   * Returns the session id or {@code null}.
   *
   * @return session id or {@code null}
   */
  @Override
  public SessionId getPrincipal() {
    return sessionId;
  }

  /**
   * Creates a new {@link BearerToken} from raw string representation.
   *
   * @param raw string representation
   *
   * @return new bearer token
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
   *
   * @return new bearer token
   */
  public static BearerToken create(@Nullable SessionId session, String rawToken) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(rawToken), "raw token is required");
    return new BearerToken(session, rawToken);
  }
}
