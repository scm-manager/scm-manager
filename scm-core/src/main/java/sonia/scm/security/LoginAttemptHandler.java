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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * @since 1.34
 */
public interface LoginAttemptHandler
{

  /**
   * This method is called before the authentication procedure is invoked.
   *
   * @param token authentication token
   */
  void beforeAuthentication(AuthenticationToken token) throws AuthenticationException;

  /**
   * Handle successful authentication.
   *
   * @param token authentication token
   * @param info successful authentication result
   */
  void onSuccessfulAuthentication(AuthenticationToken token, AuthenticationInfo info)
    throws AuthenticationException;

  /**
   * Handle unsuccessful authentication.
   *
   * @param token authentication token
   * @param info unsuccessful authentication result
   */
  void onUnsuccessfulAuthentication(AuthenticationToken token, AuthenticationInfo info)
    throws AuthenticationException;
}
