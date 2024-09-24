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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.security.AccessToken;

/**
 * Generates cookies and invalidates initialization token cookies.
 *
 * @since 2.35.0
 */
public interface InitializationCookieIssuer {

  /**
   * Creates a cookie for token authentication and attaches it to the response.
   *
   * @param request http servlet request
   * @param response http servlet response
   * @param accessToken initialization access token
   */
  void authenticateForInitialization(HttpServletRequest request, HttpServletResponse response, AccessToken accessToken);
}
