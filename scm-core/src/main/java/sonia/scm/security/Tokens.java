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
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

/**
 * Create tokens for security reasons.
 *
 * @since 1.21
 */
public final class Tokens
{

  private Tokens() {}


  /**
   * Build an {@link AuthenticationToken} for use with
   * {@link Subject#login(org.apache.shiro.authc.AuthenticationToken)}.
   *
   *
   * @param request servlet request
   * @param username username of the user to authenticate
   * @param password password of the user to authenticate
   *
   * @return authentication token
   */
  public static AuthenticationToken createAuthenticationToken(
    HttpServletRequest request, String username, String password)
  {
    return new UsernamePasswordToken(username, password,
      request.getRemoteAddr());
  }
}
