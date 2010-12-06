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
import sonia.scm.security.EncryptionHandler;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlAuthenticationHandler implements AuthenticationHandler
{

  /** Field description */
  public static final String NAME_DIRECTORY = "users";

  /** Field description */
  public static final String TYPE = "xml";

  /** the logger for XmlAuthenticationHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(XmlAuthenticationHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param userManager
   * @param encryptionHandler
   */
  @Inject
  public XmlAuthenticationHandler(UserManager userManager,
                                  EncryptionHandler encryptionHandler)
  {
    this.userManager = userManager;
    this.encryptionHandler = encryptionHandler;
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
    AuthenticationResult result = null;
    User user = userManager.get(username);

    if (user != null)
    {
      if (TYPE.equals(user.getType()))
      {
        result = authenticate(user, username, password);
      }
      else
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("{} is not an xml user", username);
        }

        result = AuthenticationResult.NOT_FOUND;
      }
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("could not find user {}", username);
      }

      result = AuthenticationResult.NOT_FOUND;
    }

    return result;
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

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param provider
   */
  @Override
  public void init(SCMContextProvider provider)
  {

    // do nothing
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getType()
  {
    return TYPE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   * @param username
   * @param password
   *
   * @return
   */
  private AuthenticationResult authenticate(User user, String username,
          String password)
  {
    AuthenticationResult result = null;
    String encryptedPassword = encryptionHandler.encrypt(password);

    if (!encryptedPassword.equalsIgnoreCase(user.getPassword()))
    {
      user = null;

      if (logger.isDebugEnabled())
      {
        logger.debug("password for user {} is wrong", username);
      }

      result = AuthenticationResult.FAILED;
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("user {} logged in successfully", username);
      }

      user.setPassword(null);
      result = new AuthenticationResult(user, AuthenticationState.SUCCESS);
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private EncryptionHandler encryptionHandler;

  /** Field description */
  private UserManager userManager;
}
