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



package sonia.scm.pam;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;
import org.jvnet.libpam.impl.CLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Extension
public class PAMAuthenticationHandler implements AuthenticationHandler
{

  /** Field description */
  public static final String TYPE = "pam";

  /** the logger for PAMAuthenticationHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(PAMAuthenticationHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param factory
   */
  @Inject
  public PAMAuthenticationHandler(StoreFactory factory)
  {
    store = factory.getStore(PAMConfig.class, TYPE);
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

    PAM pam = null;

    try
    {
      pam = new PAM(config.getServiceName());
    }
    catch (PAMException ex)
    {
      logger.warn("could not load pam module", ex);
    }

    AuthenticationResult result = AuthenticationResult.NOT_FOUND;

    if (pam != null)
    {
      if (CLibrary.libc.getpwnam(username) != null)
      {
        try
        {
          UnixUser unixUser = pam.authenticate(username, password);

          if (unixUser != null)
          {
            User user = new User(username);

            user.setAdmin(isAdmin(unixUser));
            result = new AuthenticationResult(user);
          }
        }
        catch (PAMException ex)
        {
          result = AuthenticationResult.FAILED;
        }
      }
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
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    config = store.get();

    if (config == null)
    {
      config = new PAMConfig();
      store.set(config);
    }
  }

  /**
   * Method description
   *
   */
  public void storeConfig()
  {
    store.set(config);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public PAMConfig getConfig()
  {
    return config;
  }

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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   */
  public void setConfig(PAMConfig config)
  {
    this.config = config;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param unixUser
   *
   * @return
   */
  private boolean isAdmin(UnixUser unixUser)
  {
    boolean admin = false;

    if (config.getAdminUserSet().contains(unixUser.getUserName()))
    {
      admin = true;
    }
    else
    {
      for (String group : unixUser.getGroups())
      {
        if (config.getAdminGroupSet().contains(group))
        {
          admin = true;

          break;
        }
      }
    }

    return admin;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PAMConfig config;

  /** Field description */
  private Store<PAMConfig> store;
}
