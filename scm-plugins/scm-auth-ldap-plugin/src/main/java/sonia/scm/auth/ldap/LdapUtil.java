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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

/**
 *
 * @author Sebastian Sdorra
 */
public class LdapUtil
{

  /** Field description */
  public static final String SCOPE_OBJECT = "object";

  /** Field description */
  public static final String SCOPE_ONE = "one";

  /** Field description */
  public static final String SCOPE_SUB = "sub";

  /** the logger for LdapUtil */
  private static final Logger logger = LoggerFactory.getLogger(LdapUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   */
  public static void close(Context context)
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

  /**
   * Method description
   *
   *
   * @param enm
   */
  public static void close(NamingEnumeration<?> enm)
  {
    if (enm != null)
    {
      try
      {
        enm.close();
      }
      catch (NamingException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param attributes
   * @param name
   *
   * @return
   *
   */
  public static String getAttribute(Attributes attributes, String name)
  {
    String value = null;

    try
    {
      if (Util.isNotEmpty(name))
      {
        Attribute attribute = attributes.get(name);

        if (attribute != null)
        {
          value = (String) attribute.get();
        }
      }
    }
    catch (NamingException ex)
    {
      logger.warn("could not fetch attribute ".concat(name), ex);
    }

    return value;
  }

  /**
   * Method description
   *
   *
   * @param scopeString
   *
   * @return
   */
  public static int getSearchScope(String scopeString)
  {
    int scope = SearchControls.SUBTREE_SCOPE;

    if (Util.isNotEmpty(scopeString))
    {
      scopeString = scopeString.trim();

      if (SCOPE_SUB.equalsIgnoreCase(scopeString))
      {
        scope = SearchControls.SUBTREE_SCOPE;
      }
      else if (SCOPE_ONE.equalsIgnoreCase(scopeString))
      {
        scope = SearchControls.ONELEVEL_SCOPE;
      }
      else if (SCOPE_OBJECT.equalsIgnoreCase(scopeString))
      {
        scope = SearchControls.OBJECT_SCOPE;
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("unknown scope {}, using subtree scope", scopeString);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("no search scope defined, using subtree scope");
    }

    return scope;
  }

  /**
   * Method description
   *
   *
   * @param scope
   *
   * @return
   */
  public static String getSearchScope(int scope)
  {
    String scopeString = SCOPE_SUB;

    switch (scope)
    {
      case SearchControls.ONELEVEL_SCOPE :
        scopeString = SCOPE_ONE;

        break;

      case SearchControls.OBJECT_SCOPE :
        scopeString = SCOPE_OBJECT;
    }

    return scopeString;
  }
}
