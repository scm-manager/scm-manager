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
