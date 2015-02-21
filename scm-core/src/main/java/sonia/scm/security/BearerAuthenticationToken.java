/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Token used for authentication with bearer tokens.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public class BearerAuthenticationToken implements AuthenticationToken
{

  /** Field description */
  private static final long serialVersionUID = -5005335710978534182L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new BearerAuthenticationToken
   *
   *
   * @param token bearer token
   */
  public BearerAuthenticationToken(String token)
  {
    this.token = token;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the token.
   *
   *
   * @return token
   */
  @Override
  public String getCredentials()
  {
    return token;
  }

  /**
   * Returns the username or null.
   *
   *
   * @return username or null
   */
  @Override
  public String getPrincipal()
  {
    return null;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the username.
   *
   *
   * @param username username
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  //~--- fields ---------------------------------------------------------------

  /** bearer token */
  private final String token;

  /** username */
  private String username;
}
