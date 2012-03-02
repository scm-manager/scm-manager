/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class AuthenticationResult implements Serializable
{

  /** Field description */
  public static final AuthenticationResult NOT_FOUND =
    new AuthenticationResult(AuthenticationState.NOT_FOUND);

  /** Field description */
  public static final AuthenticationResult FAILED =
    new AuthenticationResult(AuthenticationState.FAILED);

  /** Field description */
  private static final long serialVersionUID = -1318200822398893598L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param state
   */
  public AuthenticationResult(AuthenticationState state)
  {
    this.state = state;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param user
   */
  public AuthenticationResult(User user)
  {
    this.user = user;
    this.state = AuthenticationState.SUCCESS;
  }

  /**
   * Constructs ...
   *
   *
   * @param user
   * @param state
   */
  public AuthenticationResult(User user, AuthenticationState state)
  {
    this.user = user;
    this.state = state;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param user
   * @param groups
   */
  public AuthenticationResult(User user, Collection<String> groups)
  {
    this.user = user;
    this.groups = groups;
    this.state = AuthenticationState.SUCCESS;
  }

  /**
   * Constructs ...
   *
   *
   * @param user
   * @param groups
   * @param state
   */
  public AuthenticationResult(User user, Collection<String> groups,
                              AuthenticationState state)
  {
    this.user = user;
    this.groups = groups;
    this.state = state;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder out = new StringBuilder("user: ");

    if (user != null)
    {
      out.append(user.getName());
    }
    else
    {
      out.append("null");
    }

    return out.append(", state: ").append(state.toString()).toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getGroups()
  {
    return groups;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public AuthenticationState getState()
  {
    return state;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public User getUser()
  {
    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isCacheable()
  {
    return cacheable;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param cacheable
   */
  public void setCacheable(boolean cacheable)
  {
    this.cacheable = cacheable;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean cacheable = true;

  /** Field description */
  private Collection<String> groups;

  /** Field description */
  private AuthenticationState state;

  /** Field description */
  private User user;
}
