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



package sonia.scm.activedirectory.auth;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import com4j.COM4J;
import com4j.Com4jObject;
import com4j.ComException;
import com4j.ExecutionException;
import com4j.Variant;

import com4j.typelibs.activeDirectory.IADs;
import com4j.typelibs.activeDirectory.IADsGroup;
import com4j.typelibs.activeDirectory.IADsOpenDSObject;
import com4j.typelibs.activeDirectory.IADsUser;
import com4j.typelibs.ado20.ClassFactory;
import com4j.typelibs.ado20.Field;
import com4j.typelibs.ado20.Fields;
import com4j.typelibs.ado20._Command;
import com4j.typelibs.ado20._Connection;
import com4j.typelibs.ado20._Recordset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An AuthenticationHandler that uses ADSI/COM to perform authentication against
 * Active Directory with minimal configuration needed.  Currently only supported
 * on Windows with a 32-bit JVM.  Based on the Hudson active-directory plugin
 * by Kohsuke Kawaguchi.
 *
 * @author David M. Carr
 */
@Singleton
@Extension
public class ActiveDirectoryAuthenticationHandler
        implements AuthenticationHandler
{

  /** Field description */
  public static final String TYPE = "activedirectory";

  /** the logger for ActiveDirectoryAuthenticationHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(ActiveDirectoryAuthenticationHandler.class);

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

    return authenticate(username, password);
  }

  /**
   * If true, we can do ADSI/COM based look up.  Otherwise, we would need an
   * alternate approach, which has yet to be implemented.
   *
   * @return
   */
  public boolean canDoNativeAuth()
  {
    return SystemUtil.isWindows() && SystemUtil.is32bit();
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
    if (con != null)
    {
      con.close();
      con.dispose();
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
    if (!canDoNativeAuth())
    {
      if (logger.isErrorEnabled())
      {
        logger.error(
            "Currently, this plugin is only supported on Windows "
            + "with a 32-bit JVM.  Active Directory information will not be "
            + "available.");
      }

      return;
    }

    try
    {
      IADs rootDSE = COM4J.getObject(IADs.class, "LDAP://RootDSE", null);

      defaultNamingContext = (String) rootDSE.get("defaultNamingContext");
      logger.info("Active Directory domain is " + defaultNamingContext);
      con = ClassFactory.createConnection();
      con.provider("ADsDSOObject");
      con.open("Active Directory Provider", "" /* default */, "" /* default */,
               -1 /* default */);
      readDomains(rootDSE);
      logger.debug("Connected to Active Directory");
    }
    catch (ExecutionException ex)
    {
      logger.error("Failure initializing ADSI connection", ex);
    }
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

  /**
   * Method description
   *
   *
   *
   * @param domainContext
   * @param userOrGroupname
   *
   * @return
   */
  protected String getDnOfUserOrGroup(String domainContext,
          String userOrGroupname)
  {
    String dn;
    _Command cmd = ClassFactory.createCommand();

    cmd.activeConnection(con);
    cmd.commandText("<LDAP://" + domainContext + ">;(sAMAccountName="
                    + userOrGroupname + ");distinguishedName;subTree");

    _Recordset rs = cmd.execute(null, Variant.MISSING, -1 /* default */);

    if (!rs.eof())
    {
      dn = getString(rs, "distinguishedName");
    }
    else
    {
      dn = null;    // No such user or group
    }

    return dn;
  }

  //~--- methods --------------------------------------------------------------

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
    if (!canDoNativeAuth())
    {
      return null;
    }

    if (con == null)
    {
      return null;
    }

    AuthenticationResult result;
    String host = "";
    String internalName = username;
    String domainContext = defaultNamingContext;
    int index = username.indexOf("\\");

    if (index > 0)
    {
      String domain = username.substring(0, index);

      username = username.substring(index + 1);
      internalName = domain.toLowerCase().concat("/").concat(username);

      ActiveDirectoryDomain d = domainMap.get(domain.toUpperCase());

      if (d != null)
      {
        domainContext = d.getDomainContext();
        host = Util.nonNull(d.getHost());
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find domain {}", domain);
      }
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("try to autenticate user {} in context {}", username,
                   domainContext);
    }

    String dn = getDnOfUserOrGroup(domainContext, username);

    if (logger.isDebugEnabled())
    {
      logger.debug("found user at {}", dn);
    }

    // now we got the DN of the user
    IADsOpenDSObject dso = COM4J.getObject(IADsOpenDSObject.class, "LDAP:",
                             null);

    try
    {
      if (Util.isNotEmpty(host))
      {
        host = host.concat("/");
      }

      IADsUser usr = dso.openDSObject("LDAP://".concat(host).concat(dn), dn,
                                      password,
                                      0).queryInterface(IADsUser.class);

      if (usr != null)
      {
        if (!usr.accountDisabled())
        {
          User user = new User(internalName, usr.fullName(),
                               usr.emailAddress());

          user.setType(TYPE);
          result = new AuthenticationResult(user, getGroups(usr));
        }
        else
        {    // Account disabled
          result = AuthenticationResult.FAILED;
        }
      }
      else
      {      // the user name was in fact a group
        result = AuthenticationResult.NOT_FOUND;
      }
    }
    catch (ComException e)
    {
      result = AuthenticationResult.FAILED;
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param rootDSE
   */
  private void readDomains(IADs rootDSE)
  {
    try
    {
      String configNC = (String) rootDSE.get("configurationNamingContext");

      if (Util.isNotEmpty(configNC))
      {
        _Command cmd = ClassFactory.createCommand();

        cmd.activeConnection(con);
        cmd.commandText("<LDAP://" + configNC
                        + ">;(NETBIOSName=*);cn,dnsRoot,ncname;subTree");

        _Recordset rs = cmd.execute(null, Variant.MISSING, -1 /* default */);

        while (!rs.eof())
        {
          String cn = getString(rs, "cn");
          String dn = getString(rs, "ncname");
          String host = getFirstString(rs, "dnsRoot");

          if (Util.isNotEmpty(cn) && Util.isNotEmpty(dn))
          {
            if (logger.isInfoEnabled())
            {
              logger.info("found domain: {}, {}, {}", new Object[] { cn, dn,
                      host });
            }

            domainMap.put(cn, new ActiveDirectoryDomain(cn, dn, host));
          }

          rs.moveNext();
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find a valid configurationNamingContext");
      }
    }
    catch (Exception ex)
    {
      logger.error("could not read domains", ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Get the first String of a multivalue recordset item
   *
   *
   * @param rs
   * @param item
   *
   * @return the first item of a recordset
   */
  private String getFirstString(_Recordset rs, String item)
  {
    String result = null;
    Object[] values = (Object[]) getObject(rs, item);

    if (Util.isNotEmpty(values))
    {
      Object value = values[0];

      if (value != null)
      {
        result = value.toString();
      }
    }

    return result;
  }

  /**
   * Fetch all groupnames of a user
   *
   *
   * @param usr
   *
   * @return
   */
  private Collection<String> getGroups(IADsUser usr)
  {
    Set<String> groups = new TreeSet<String>();

    for (Com4jObject g : usr.groups())
    {
      IADsGroup grp = g.queryInterface(IADsGroup.class);

      // cut "CN=" and make that the role name
      String groupName = grp.name().substring(3);

      groups.add(groupName);
    }

    return groups;
  }

  /**
   * Method description
   *
   *
   * @param rs
   * @param item
   *
   * @return
   */
  private Object getObject(_Recordset rs, String item)
  {
    Object value = null;
    Fields fields = rs.fields();

    if (fields != null)
    {
      Field field = fields.item(item);

      if (field != null)
      {
        value = field.value();
      }
    }

    return value;
  }

  /**
   * Method description
   *
   *
   * @param rs
   * @param item
   *
   * @return
   */
  private String getString(_Recordset rs, String item)
  {
    String result = null;
    Object value = getObject(rs, item);

    if (value != null)
    {
      result = value.toString();
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private _Connection con;

  /** Field description */
  private String defaultNamingContext;

  /** Field description */
  private Map<String, ActiveDirectoryDomain> domainMap =
    new HashMap<String, ActiveDirectoryDomain>();
}
