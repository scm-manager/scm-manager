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
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.security.EncryptionHandler;
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
public class ChainAuthenticatonManager extends AbstractAuthenticationManager
{

  /** Field description */
  public static final String CACHE_NAME = "sonia.cache.auth";

  /** the logger for ChainAuthenticatonManager */
  private static final Logger logger =
    LoggerFactory.getLogger(ChainAuthenticatonManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param authenticationHandlerSet
   * @param encryptionHandler
   * @param cacheManager
   */
  @Inject
  public ChainAuthenticatonManager(
          Set<AuthenticationHandler> authenticationHandlerSet,
          EncryptionHandler encryptionHandler, CacheManager cacheManager)
  {
    AssertUtil.assertIsNotEmpty(authenticationHandlerSet);
    AssertUtil.assertIsNotNull(cacheManager);
    this.authenticationHandlerSet = authenticationHandlerSet;
    this.encryptionHandler = encryptionHandler;
    this.cache = cacheManager.getCache(String.class,
                                       AuthenticationCacheValue.class,
                                       CACHE_NAME);
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
  public AuthenticationResult authenticate(HttpServletRequest request,
          HttpServletResponse response, String username, String password)
  {
    AssertUtil.assertIsNotEmpty(username);
    AssertUtil.assertIsNotEmpty(password);

    String encryptedPassword = encryptionHandler.encrypt(password);
    AuthenticationResult ar = getCached(username, encryptedPassword);

    if (ar == null)
    {
      ar = doAuthentication(request, response, username, password);

      if ((ar != null) && ar.isCacheable())
      {
        cache.put(username,
                  new AuthenticationCacheValue(ar, encryptedPassword));
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("authenticate {} via cache", username);
    }

    return ar;
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
  private AuthenticationResult doAuthentication(HttpServletRequest request,
          HttpServletResponse response, String username, String password)
  {
    AuthenticationResult ar = null;

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
            User user = result.getUser();

            user.setType(authenticator.getType());
            ar = result;

            // notify authentication listeners
            fireAuthenticationEvent(request, response, user);
          }

          break;
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    return ar;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   * @param encryptedPassword
   *
   * @return
   */
  private AuthenticationResult getCached(String username,
          String encryptedPassword)
  {
    AuthenticationResult result = null;
    AuthenticationCacheValue value = cache.get(username);

    if (value != null)
    {
      String cachedPassword = value.password;

      if (cachedPassword.equals(encryptedPassword))
      {
        result = value.authenticationResult;
      }
    }

    return result;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2011-01-15
   * @author         Sebastian Sdorra
   */
  private static class AuthenticationCacheValue
  {

    /**
     * Constructs ...
     *
     *
     *
     * @param ar
     * @param password
     */
    public AuthenticationCacheValue(AuthenticationResult ar, String password)
    {
      this.authenticationResult =
        new AuthenticationResult(ar.getUser().clone(), ar.getGroups(),
                                 ar.getState());
      this.password = password;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private AuthenticationResult authenticationResult;

    /** Field description */
    private String password;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<AuthenticationHandler> authenticationHandlerSet;

  /** Field description */
  private Cache<String, AuthenticationCacheValue> cache;

  /** Field description */
  private EncryptionHandler encryptionHandler;
}
