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



package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.text.MessageFormat;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sonia.scm.user.User;

/**
 *
 * @author Thorsten Ludewig
 */
@Singleton
@Extension
public class LDAPAuthenticationHandler implements AuthenticationHandler
{

  /** Field description */
  public static final String TYPE = "ldap";

  /** the logger for PAMAuthenticationHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(LDAPAuthenticationHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param factory
   */
  @Inject
  public LDAPAuthenticationHandler(StoreFactory factory)
  {
    store = factory.getStore(LDAPConfig.class, TYPE);
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

    AuthenticationResult result = AuthenticationResult.NOT_FOUND;
    DirContext context = null;

    try
    {
      context = new InitialDirContext(ldapProperties);

      SearchControls searchControls = new SearchControls();

      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      searchControls.setCountLimit(1);
      searchControls.setReturningAttributes(new String[] {
        config.getAttributeNameId(),
        config.getAttributeNameFullname(), config.getAttributeNameMail() });

      String filter = MessageFormat.format(config.getSearchFilter(), username);
      String baseDn = config.getUnitPeople() + "," + config.getBaseDn();
      NamingEnumeration<SearchResult> searchResult = context.search(baseDn,
                                                       filter, searchControls);

      if (searchResult.hasMore())
      {
        result = AuthenticationResult.FAILED;

        SearchResult sr = searchResult.next();
        String userDn = sr.getName() + "," + baseDn;
        Properties userProperties = new Properties(ldapProperties);

        userProperties.put(Context.SECURITY_PRINCIPAL, userDn);
        userProperties.put(Context.SECURITY_CREDENTIALS, password);

        DirContext userContext = null;

        try
        {
          userContext = new InitialDirContext(userProperties);

          User user = new User();
          Attributes userAttributes = sr.getAttributes();
          user.setName((String)userAttributes.get(config.getAttributeNameId()).get());
          user.setDisplayName((String)userAttributes.get(config.getAttributeNameFullname()).get());
          user.setMail((String)userAttributes.get(config.getAttributeNameMail()).get());
          user.setType(TYPE);
          result = new AuthenticationResult(user);
        }
        catch (NamingException ex)
        {
          logger.trace(ex.getMessage(), ex);
        }
        finally
        {
          if (userContext != null)
          {
            try
            {
              userContext.close();
            }
            catch (NamingException ex)
            {
              logger.error(ex.getMessage(), ex);
            }
          }
        }
      }

      searchResult.close();
    }
    catch (NamingException ex)
    {
      logger.error(ex.getMessage(), ex);
    }
    finally
    {
      if (context != null)
      {
        try
        {
          context.close();
        }
        catch (NamingException ex)
        {
          logger.error(ex.getMessage(), ex);
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
      config = new LDAPConfig();
      store.set(config);
    }

    buildLdapProperties();
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
  public LDAPConfig getConfig()
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
  public void setConfig(LDAPConfig config)
  {
    this.config = config;
    buildLdapProperties();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void buildLdapProperties()
  {
    ldapProperties = new Properties();
    ldapProperties.put(Context.INITIAL_CONTEXT_FACTORY,
                       "com.sun.jndi.ldap.LdapCtxFactory");
    ldapProperties.put(Context.PROVIDER_URL, config.getHostUrl());
    ldapProperties.put(Context.SECURITY_AUTHENTICATION, "simple");

    /*
     * if( contextSecurityProtocol.equalsIgnoreCase( "ssl" ) )
     * {
     * ldapContextProperties.put( Context.SECURITY_PROTOCOL, "ssl" );
     * ldapContextProperties.put( "java.naming.ldap.factory.socket",
     *   "sonia.net.ssl.SSLSocketFactory" );
     * }
     */
    ldapProperties.put(Context.SECURITY_PRINCIPAL, config.getConnectionDn());
    ldapProperties.put(Context.SECURITY_CREDENTIALS,
                       config.getConnectionPassword());
    ldapProperties.put("java.naming.ldap.version", "3");
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private LDAPConfig config;

  /** Field description */
  private Properties ldapProperties;

  /** Field description */
  private Store<LDAPConfig> store;
}
