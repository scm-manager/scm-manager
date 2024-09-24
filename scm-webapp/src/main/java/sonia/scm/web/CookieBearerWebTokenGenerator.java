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


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.plugin.Extension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;

/**
 * Creates an {@link BearerToken} from the {@link HttpUtil#COOKIE_BEARER_AUTHENTICATION}
 * cookie.
 *
 * @since 2.0.0
 */
@Extension
public class CookieBearerWebTokenGenerator implements WebTokenGenerator
{

  /**
   * Creates an {@link BearerToken} from the {@link HttpUtil#COOKIE_BEARER_AUTHENTICATION}
   * cookie.
   *
   * @param request http servlet request
   *
   * @return {@link BearerToken} or {@code null}
   */
  @Override
  public BearerToken createToken(HttpServletRequest request) {
    BearerToken token = null;
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (HttpUtil.COOKIE_BEARER_AUTHENTICATION.equals(cookie.getName())) {
          token = BearerToken.create(SessionId.from(request).orElse(null), cookie.getValue());

          break;
        }
      }
    }

    return token;
  }
}
