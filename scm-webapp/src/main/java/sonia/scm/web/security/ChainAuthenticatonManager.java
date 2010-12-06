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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ChainAuthenticatonManager implements AuthenticationManager
{

  /** the logger for ChainAuthenticatonManager */
  private static final Logger logger =
    LoggerFactory.getLogger(ChainAuthenticatonManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param authenticationHandlerSet
   */
  @Inject
  public ChainAuthenticatonManager(
          Set<AuthenticationHandler> authenticationHandlerSet)
  {
    AssertUtil.assertIsNotEmpty(authenticationHandlerSet);
    this.authenticationHandlerSet = authenticationHandlerSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param username
   * @param password
   *
   * @return
   */
  @Override
  public User authenticate(HttpServletRequest request,
                           HttpServletResponse response, String username,
                           String password)
  {
    User user = null;

    for (AuthenticationHandler authenticator : authenticationHandlerSet)
    {
      try
      {
        AuthenticationResult result = authenticator.authenticate(request,
                                        response, username, password);

        if (logger.isDebugEnabled())
        {
          logger.debug("authenticator {} ends with result, {}",
                       authenticator.getClass().getName(), result);
        }

        if ((result != null) && (result.getState() != null)
            && (result.getState().isSuccessfully()
                || (result.getState() == AuthenticationState.FAILED)))
        {
          if (result.getState().isSuccessfully() && (result.getUser() != null))
          {
            user = result.getUser();
            user.setType(authenticator.getType());
          }

          break;
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    return user;
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    for (AuthenticationHandler authenticator : authenticationHandlerSet)
    {
      IOUtil.close(authenticator);
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    for (AuthenticationHandler authenticator : authenticationHandlerSet)
    {
      authenticator.init(context);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<AuthenticationHandler> authenticationHandlerSet;
}
