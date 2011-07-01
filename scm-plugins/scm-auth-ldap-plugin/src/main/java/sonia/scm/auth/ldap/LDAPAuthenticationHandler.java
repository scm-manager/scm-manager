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
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Thorsten Ludewig
 */
@Singleton
@Extension
public class LDAPAuthenticationHandler implements AuthenticationHandler
{

  /** Field description */
  public static final String ATTRIBUTE_GROUP_NAME = "cn";

  /** Field description */
  public static final String FILTER_GROUP =
    "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))";

  /** Field description */
  public static final String SEARCHTYPE_GROUP = "group";

  /** Field description */
  public static final String SEARCHTYPE_USER = "user";

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
    AuthenticationResult result = AuthenticationResult.NOT_FOUND;

    if (config.isEnabled())
    {
      AssertUtil.assertIsNotEmpty(username);
      AssertUtil.assertIsNotEmpty(password);
      result = authenticate(username, password);
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("ldap plugin is disabled");
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
   *
   * @param list
   * @param attribute
   */
  private void appendAttribute(List<String> list, String attribute)
  {
    if (Util.isNotEmpty(attribute))
    {
      list.add(attribute);
    }
  }

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   *
   * @return
   */
  private AuthenticationResult authenticate(String username, String password)
  {
    AuthenticationResult result = AuthenticationResult.NOT_FOUND;
    DirContext bindContext = null;

    try
    {
      bindContext = createBindContext();

      if (bindContext != null)
      {
        SearchResult searchResult = getUserSearchResult(bindContext, username);

        if (searchResult != null)
        {
          result = AuthenticationResult.FAILED;

          String userDN = searchResult.getNameInNamespace();

          if (authenticateUser(userDN, password))
          {
            Attributes attributes = searchResult.getAttributes();
            User user = createUser(attributes);
            Set<String> groups = new HashSet<String>();

            fetchGroups(bindContext, groups, userDN);
            getGroups(attributes, groups);
            result = new AuthenticationResult(user, groups);
          }    // password wrong ?
        }      // user not found
      }        // no bind context available
    }
    finally
    {
      LdapUtil.close(bindContext);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param userDN
   * @param password
   *
   * @return
   */
  private boolean authenticateUser(String userDN, String password)
  {
    boolean authenticated = false;
    Hashtable<String, String> userProperties = new Hashtable<String,
                                                 String>(ldapProperties);

    userProperties.put(Context.SECURITY_PRINCIPAL, userDN);
    userProperties.put(Context.SECURITY_CREDENTIALS, password);

    DirContext userContext = null;

    try
    {
      userContext = new InitialDirContext(userProperties);
      authenticated = true;

      if (logger.isDebugEnabled())
      {
        logger.debug("user {} successfully authenticated", userDN);
      }
    }
    catch (NamingException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("authentication failed for user ".concat(userDN), ex);
      }
      else if (logger.isWarnEnabled())
      {
        logger.debug("authentication failed for user {}", userDN);
      }
    }
    finally
    {
      LdapUtil.close(userContext);
    }

    return authenticated;
  }

  /**
   * Method description
   *
   */
  private void buildLdapProperties()
  {
    ldapProperties = new Hashtable<String, String>();
    ldapProperties.put(Context.INITIAL_CONTEXT_FACTORY,
                       "com.sun.jndi.ldap.LdapCtxFactory");
    ldapProperties.put(Context.PROVIDER_URL, config.getHostUrl());
    ldapProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
    ldapProperties.put(Context.SECURITY_PRINCIPAL, config.getConnectionDn());
    ldapProperties.put(Context.SECURITY_CREDENTIALS,
                       config.getConnectionPassword());
    ldapProperties.put("java.naming.ldap.version", "3");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private DirContext createBindContext()
  {
    DirContext context = null;

    try
    {
      context = new InitialDirContext(ldapProperties);
    }
    catch (NamingException ex)
    {
      logger.error(
          "could not bind to ldap with dn ".concat(config.getConnectionDn()),
          ex);
    }

    return context;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String createGroupSearchBaseDN()
  {
    return createSearchBaseDN(SEARCHTYPE_GROUP, config.getUnitGroup());
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param prefix
   *
   * @return
   */
  private String createSearchBaseDN(String type, String prefix)
  {
    String dn = null;

    if (Util.isNotEmpty(config.getBaseDn()))
    {
      if (Util.isNotEmpty(prefix))
      {
        dn = prefix.concat(",").concat(config.getBaseDn());
      }
      else
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("no prefix for {} defined, using basedn for search",
                       type);
        }

        dn = config.getBaseDn();
      }

      if (logger.isDebugEnabled())
      {
        logger.debug("saarch base for {} search: {}", type, dn);
      }
    }
    else
    {
      logger.error("no basedn defined");
    }

    return dn;
  }

  /**
   * Method description
   *
   *
   * @param attributes
   *
   * @return
   */
  private User createUser(Attributes attributes)
  {
    User user = new User();

    user.setName(LdapUtil.getAttribute(attributes,
                                       config.getAttributeNameId()));
    user.setDisplayName(LdapUtil.getAttribute(attributes,
            config.getAttributeNameFullname()));
    user.setMail(LdapUtil.getAttribute(attributes,
                                       config.getAttributeNameMail()));
    user.setType(TYPE);

    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String createUserSearchBaseDN()
  {
    return createSearchBaseDN(SEARCHTYPE_USER, config.getUnitPeople());
  }

  /**
   * Method description
   *
   *
   * @param username
   *
   * @return
   */
  private String createUserSearchFilter(String username)
  {
    String filter = null;

    if (Util.isNotEmpty(config.getSearchFilter()))
    {
      filter = MessageFormat.format(config.getSearchFilter(), username);

      if (logger.isDebugEnabled())
      {
        logger.debug("search-filter for user search: {}", filter);
      }
    }
    else
    {
      logger.error("search filter not defined");
    }

    return filter;
  }

  /**
   * Method description
   *
   *
   * @param context
   * @param groups
   * @param userDN
   */
  private void fetchGroups(DirContext context, Set<String> groups,
                           String userDN)
  {
    NamingEnumeration<SearchResult> searchResultEnm = null;

    try
    {

      // read group of unique names
      SearchControls searchControls = new SearchControls();

      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      searchControls.setReturningAttributes(new String[] { "cn" });

      String filter = MessageFormat.format(FILTER_GROUP, userDN);

      if (logger.isDebugEnabled())
      {
        logger.debug("using filter {} for group search", filter);
      }

      String searchDN = createGroupSearchBaseDN();

      searchResultEnm = context.search(searchDN, filter, searchControls);

      while (searchResultEnm.hasMore())
      {
        SearchResult searchResult = searchResultEnm.next();
        Attributes groupAttributes = searchResult.getAttributes();
        String name = LdapUtil.getAttribute(groupAttributes,
                        ATTRIBUTE_GROUP_NAME);

        if (Util.isNotEmpty(name))
        {
          groups.add(name);
        }
      }
    }
    catch (NamingException ex)
    {
      logger.debug("groupOfUniqueNames not found", ex);
    }
    finally
    {
      LdapUtil.close(searchResultEnm);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param attributes
   * @param groups
   *
   */
  private void getGroups(Attributes attributes, Set<String> groups)
  {
    NamingEnumeration<?> userGroupsEnm = null;

    try
    {
      Attribute groupsAttribute =
        attributes.get(config.getAttributeNameGroup());

      if (groupsAttribute != null)
      {
        userGroupsEnm = (NamingEnumeration<?>) groupsAttribute.getAll();

        while (userGroupsEnm.hasMore())
        {
          groups.add((String) userGroupsEnm.next());
        }
      }
      else
      {
        logger.info("user has no group attributes assigned");
      }
    }
    catch (NamingException ex)
    {
      logger.error("could not read group attribute", ex);
    }
    finally
    {
      LdapUtil.close(userGroupsEnm);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String[] getReturnAttributes()
  {
    List<String> list = new ArrayList<String>();

    appendAttribute(list, config.getAttributeNameId());
    appendAttribute(list, config.getAttributeNameFullname());
    appendAttribute(list, config.getAttributeNameMail());
    appendAttribute(list, config.getAttributeNameMail());

    return list.toArray(new String[list.size()]);
  }

  /**
   * Method description
   *
   *
   * @param bindContext
   * @param username
   *
   * @return
   */
  private SearchResult getUserSearchResult(DirContext bindContext,
          String username)
  {
    SearchResult result = null;

    if (bindContext != null)
    {
      NamingEnumeration<SearchResult> searchResultEnm = null;

      try
      {
        SearchControls searchControls = new SearchControls();
        int scope = LdapUtil.getSearchScope(config.getSearchScope());

        if (logger.isDebugEnabled())
        {
          logger.debug("using scope {} for user search",
                       LdapUtil.getSearchScope(scope));
        }

        searchControls.setSearchScope(scope);
        searchControls.setCountLimit(1);
        searchControls.setReturningAttributes(getReturnAttributes());

        String filter = createUserSearchFilter(username);

        if (filter != null)
        {
          String baseDn = createUserSearchBaseDN();

          if (baseDn != null)
          {
            searchResultEnm = bindContext.search(baseDn, filter,
                    searchControls);

            if (searchResultEnm.hasMore())
            {
              result = searchResultEnm.next();
            }
            else if (logger.isWarnEnabled())
            {
              logger.warn("no user with username {} found", username);
            }
          }
        }
      }
      catch (NamingException ex)
      {
        logger.error("exception occured during user search", ex);
      }
      finally
      {
        LdapUtil.close(searchResultEnm);
      }
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private LDAPConfig config;

  /** Field description */
  private Hashtable<String, String> ldapProperties;

  /** Field description */
  private Store<LDAPConfig> store;
}
