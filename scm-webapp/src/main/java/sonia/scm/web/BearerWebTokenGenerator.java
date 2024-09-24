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

package sonia.scm.web;


import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.plugin.Extension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;

/**
 * Creates a {@link BearerToken} from an authorization header with
 * bearer authorization.
 *
 * @since 2.0.0
 */
@Extension
public class BearerWebTokenGenerator extends SchemeBasedWebTokenGenerator
{

  /**
   * Creates a {@link BearerToken} from an authorization header
   * with bearer authorization.
   *
   * @param request http servlet request
   * @param scheme authorization scheme
   * @param authorization authorization payload
   *
   * @return {@link BearerToken} or {@code null}
   */
  @Override
  protected BearerToken createToken(HttpServletRequest request,
    String scheme, String authorization)
  {
    BearerToken token = null;

    if (HttpUtil.AUTHORIZATION_SCHEME_BEARER.equalsIgnoreCase(scheme)) {
      token = BearerToken.create(SessionId.from(request).orElse(null), authorization);
    }

    return token;
  }
}
