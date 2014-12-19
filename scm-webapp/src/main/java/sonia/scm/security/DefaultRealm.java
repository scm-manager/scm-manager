/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupNames;
import sonia.scm.plugin.Extension;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Sebastian Sdorra
 *
 * @since 2.0.0
 */
@Extension
@Singleton
public class DefaultRealm extends AuthorizingRealm
{

  /** Field description */
  @VisibleForTesting
  static final String REALM = "DefaultRealm";

  /**
   *   the logger for DefaultRealm
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultRealm.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param service
   * @param collector
   * @param userDAO
   * @param groupDAO
   */
  @Inject
  public DefaultRealm(PasswordService service,
    AuthorizationCollector collector, UserDAO userDAO, GroupDAO groupDAO)
  {
    this.collector = collector;
    this.userDAO = userDAO;
    this.groupDAO = groupDAO;

    PasswordMatcher matcher = new PasswordMatcher();

    matcher.setPasswordService(service);
    setCredentialsMatcher(matcher);
    setAuthenticationTokenClass(UsernamePasswordToken.class);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param token
   *
   * @return
   *
   * @throws AuthenticationException
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(
    AuthenticationToken token)
    throws AuthenticationException
  {
    checkArgument(token instanceof UsernamePasswordToken, "%s is required",
      UsernamePasswordToken.class);

    UsernamePasswordToken upt = (UsernamePasswordToken) token;
    String principal = upt.getUsername();

    checkArgument(!Strings.isNullOrEmpty(principal), "username is required");

    logger.debug("try to authenticate {}", principal);

    User user = userDAO.get(principal);

    if (user == null)
    {
      //J-
      throw new UnknownAccountException(
        String.format("unknown account %s", principal)
      );
      //J+
    }

    if (!user.isActive())
    {
      //J-
      throw new DisabledAccountException(
        String.format("account %s is disabled", principal)
      );
      //J+
    }

    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    collection.add(principal, REALM);
    collection.add(user, REALM);
    collection.add(collectGroups(principal), REALM);

    return new SimpleAuthenticationInfo(collection, user.getPassword());
  }

  /**
   * Method description
   *
   *
   * @param principals
   *
   * @return
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(
    PrincipalCollection principals)
  {
    return collector.collect(principals);
  }

  /**
   * Method description
   *
   *
   * @param principal
   *
   * @return
   */
  private GroupNames collectGroups(String principal)
  {
    Builder<String> builder = ImmutableSet.builder();

    builder.add(GroupNames.AUTHENTICATED);

    for (Group group : groupDAO.getAll())
    {
      if (group.isMember(principal))
      {
        builder.add(group.getName());
      }
    }

    GroupNames groups = new GroupNames(builder.build());

    logger.debug("collected following groups for principal {}: {}", principal,
      groups);

    return groups;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final AuthorizationCollector collector;

  /** Field description */
  private final GroupDAO groupDAO;

  /** Field description */
  private final UserDAO userDAO;
}
