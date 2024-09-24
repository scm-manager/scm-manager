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

package sonia.scm.initialization;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import sonia.scm.plugin.Extension;
import sonia.scm.web.WebTokenGenerator;

@Extension
public class InitializationWebTokenGenerator implements WebTokenGenerator {

  public static final String INIT_TOKEN_HEADER = "X-SCM-Init-Token";

  @Override
  public AuthenticationToken createToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    AuthenticationToken token = null;
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(INIT_TOKEN_HEADER)) {
          token = new InitializationToken(cookie.getValue(), "SCM_INIT");
        }
      }
    }
    return token;
  }
}
