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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Generates cookies and invalidates access token cookies.
 *
 * @since 2.0.0
 */
public interface AccessTokenCookieIssuer {

  /**
   * Creates a cookie for token authentication and attaches it to the response.
   */
  void authenticate(HttpServletRequest request, HttpServletResponse response, AccessToken accessToken);

  /**
   * Invalidates the authentication cookie.
   */
  void invalidate(HttpServletRequest request, HttpServletResponse response);

}
