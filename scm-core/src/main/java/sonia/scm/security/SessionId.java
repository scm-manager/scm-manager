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
