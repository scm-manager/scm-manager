/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.security;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEvent;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserEventHack;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.util.Util;
import sonia.scm.web.security.AuthenticationResult;

/**
 *
 * @author Sebastian Sdorra
 */
public class AuthenticationInfoCollector {
 
  private static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";
  
  /**
   * the logger for AuthenticationInfoCollector
   */
  private static final Logger logger = LoggerFactory.getLogger(AuthenticationInfoCollector.class);
  
  private final ScmConfiguration configuration;
  private final UserManager userManager;
  private final GroupManager groupManager; 
  private final UserDAO userDAO;
  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public AuthenticationInfoCollector(ScmConfiguration configuration, UserManager userManager, GroupManager groupManager,
    UserDAO userDAO, Provider<HttpServletRequest> requestProvider) {
    this.configuration = configuration;
    this.userManager = userManager;
    this.groupManager = groupManager;
    this.userDAO = userDAO;
    this.requestProvider = requestProvider;
  }
  
  AuthenticationInfo createAuthenticationInfo(UsernamePasswordToken token, AuthenticationResult result) {
    User user = result.getUser();
    Collection<String> groups = authenticate(requestProvider.get(), new String(token.getPassword()), result);

    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    /*
     * the first (primary) principal should be a unique identifier
     */
    collection.add(user.getId(), ScmRealm.NAME);
    collection.add(user, ScmRealm.NAME);
    collection.add(new GroupNames(groups), ScmRealm.NAME);

    return new SimpleAuthenticationInfo(collection, token.getPassword());
  }
  
  
  private Set<String> authenticate(HttpServletRequest request, String password, AuthenticationResult ar) {
    Set<String> groupSet = null;
    User user = ar.getUser();

    try
    {
      groupSet = createGroupSet(ar);

      // check for admin user
      checkForAuthenticatedAdmin(user, groupSet);

      // store user
      User dbUser = userDAO.get(user.getName());

      if (dbUser != null)
      {
        checkDBForAdmin(user, dbUser);
        checkDBForActive(user, dbUser);
      }

      // create new user
      else if (user.isValid())
      {
        user.setCreationDate(System.currentTimeMillis());

        // TODO find a better way
        UserEventHack.fireEvent(userManager, user, HandlerEvent.BEFORE_CREATE);
        userDAO.add(user);
        UserEventHack.fireEvent(userManager, user, HandlerEvent.CREATE);
      }
      else if (logger.isErrorEnabled())
      {
        logger.error("could not create user {}, beacause it is not valid",
          user.getName());
      }

      if (user.isActive())
      {

        if (logger.isDebugEnabled())
        {
          logGroups(user, groupSet);
        }

        // store encrypted credentials in session
        String credentials = user.getName();

        if (Util.isNotEmpty(password))
        {
          credentials = credentials.concat(":").concat(password);
        }

        credentials = CipherUtil.getInstance().encode(credentials);
        request.getSession(true).setAttribute(SCM_CREDENTIALS, credentials);
      }
      else
      {

        String msg = "user ".concat(user.getName()).concat(" is deactivated");

        if (logger.isWarnEnabled())
        {
          logger.warn(msg);
        }

        throw new DisabledAccountException(msg);

      }
    }
    catch (IOException | UserException ex)
    {
      logger.error("authentication failed", ex);

      throw new AuthenticationException("authentication failed", ex);
    }

    return groupSet;
  }

  private void checkDBForActive(User user, User dbUser)
  {
    // user is deactivated by database
    if (!dbUser.isActive())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("user {} is marked as deactivated by local database",
          user.getName());
      }

      user.setActive(false);
    }
  }

  private void checkDBForAdmin(User user, User dbUser) throws UserException, IOException
  {

    // if database user is an admin, set admin for the current user
    if (dbUser.isAdmin())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("user {} of type {} is marked as admin by local database",
          user.getName(), user.getType());
      }

      user.setAdmin(true);
    }

    // modify existing user, copy properties except password and admin
    if (user.copyProperties(dbUser, false))
    {
      user.setLastModified(System.currentTimeMillis());
      UserEventHack.fireEvent(userManager, user, HandlerEvent.BEFORE_MODIFY);
      userDAO.modify(user);
      UserEventHack.fireEvent(userManager, user, HandlerEvent.MODIFY);
    }
  }

  private void checkForAuthenticatedAdmin(User user, Set<String> groupSet)
  {
    if (!user.isAdmin())
    {
      user.setAdmin(isAdmin(user, groupSet));

      if (logger.isDebugEnabled() && user.isAdmin())
      {
        logger.debug("user {} is marked as admin by configuration",
          user.getName());
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("authenticator {} marked user {} as admin", user.getType(),
        user.getName());
    }
  }

  private Set<String> createGroupSet(AuthenticationResult ar)
  {
    Set<String> groupSet = Sets.newHashSet();

    // add group for all authenticated users
    groupSet.add(GroupNames.AUTHENTICATED);

    // load external groups
    Collection<String> extGroups = ar.getGroups();

    if (extGroups != null)
    {
      groupSet.addAll(extGroups);
    }

    // load internal groups
    loadGroups(ar.getUser(), groupSet);

    return groupSet;
  }

  private void loadGroups(User user, Set<String> groupSet)
  {
    Collection<Group> groupCollection = groupManager.getGroupsForMember(user.getName());

    if (groupCollection != null)
    {
      for (Group group : groupCollection)
      {
        groupSet.add(group.getName());
      }
    }
  }

  private void logGroups(User user, Set<String> groups)
  {
    StringBuilder msg = new StringBuilder("user ");

    msg.append(user.getName());

    if (Util.isNotEmpty(groups))
    {
      msg.append(" is member of ");

      Joiner.on(", ").appendTo(msg, groups);
    }
    else
    {
      msg.append(" is not a member of a group");
    }

    logger.debug(msg.toString());
  }

  private boolean isAdmin(User user, Collection<String> groups)
  {
    boolean result = false;
    Set<String> adminUsers = configuration.getAdminUsers();

    if (adminUsers != null)
    {
      result = adminUsers.contains(user.getName());
    }

    if (!result)
    {
      Set<String> adminGroups = configuration.getAdminGroups();

      if (adminGroups != null)
      {
        result = Util.containsOne(adminGroups, groups);
      }
    }

    return result;
  }

  
}
