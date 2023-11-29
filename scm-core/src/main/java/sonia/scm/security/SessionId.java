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
import jakarta.servlet.http.HttpServletRequest;
import lombok.EqualsAndHashCode;
import sonia.scm.util.HttpUtil;

import java.io.Serializable;
import java.util.Optional;

/**
 * Client side session id.
 */
@EqualsAndHashCode
public final class SessionId implements Serializable {

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
