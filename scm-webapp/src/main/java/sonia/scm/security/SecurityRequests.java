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

import java.util.regex.Pattern;

import static sonia.scm.api.v2.resources.ScmPathInfo.REST_API_PATH;

public final class SecurityRequests {

  private static final Pattern URI_LOGIN_PATTERN = Pattern.compile(REST_API_PATH + "(?:/v2)?/auth/access_token");
  private static final Pattern URI_INDEX_PATTERN = Pattern.compile(REST_API_PATH + "/v2/?");

  private SecurityRequests() {}

  public static boolean isAuthenticationRequest(HttpServletRequest request) {
    String uri = request.getRequestURI().substring(request.getContextPath().length());
    return isAuthenticationRequest(uri) && !isLogoutMethod(request);
  }

  private static boolean isLogoutMethod(HttpServletRequest request) {
    return "DELETE".equals(request.getMethod());
  }

  public static boolean isAuthenticationRequest(String uri) {
    return URI_LOGIN_PATTERN.matcher(uri).matches();
  }

  public static boolean isIndexRequest(HttpServletRequest request) {
    String uri = request.getRequestURI().substring(request.getContextPath().length());
    return isIndexRequest(uri);
  }

  public static boolean isIndexRequest(String uri) {
    return URI_INDEX_PATTERN.matcher(uri).matches();
  }

}
