/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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

import com.google.common.base.Objects;

import org.apache.shiro.authc.AuthenticationToken;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmAuthenticationToken implements AuthenticationToken
{

  /** Field description */
  private static final long serialVersionUID = -3208692400029843828L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param request
   * @param response
   * @param username
   * @param password
   */
  public ScmAuthenticationToken(HttpServletRequest request,
    HttpServletResponse response, String username, String password)
  {
    this.request = request;
    this.response = response;
    this.username = username;
    this.password = password;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final ScmAuthenticationToken other = (ScmAuthenticationToken) obj;

    return Objects.equal(request, other.request)
      && Objects.equal(response, other.response)
      && Objects.equal(username, other.username)
      && Objects.equal(password, other.password);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(request, response, username, password);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
                  .add("request", request)
                  .add("response", response)
                  .add("username", username)
                  .add("password", "xxx")
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getCredentials()
  {
    return password;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getPrincipal()
  {
    return username;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public HttpServletRequest getRequest()
  {
    return request;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public HttpServletResponse getResponse()
  {
    return response;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String password;

  /** Field description */
  private HttpServletRequest request;

  /** Field description */
  private HttpServletResponse response;

  /** Field description */
  private String username;
}
