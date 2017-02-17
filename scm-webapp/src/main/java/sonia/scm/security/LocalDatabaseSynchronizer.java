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

import com.google.inject.Inject;
import java.util.Set;
import org.apache.shiro.authc.DisabledAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEvent;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserEventHack;
import sonia.scm.user.UserManager;

/**
 * Checks and synchronizes authenticated users with local database.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
public class LocalDatabaseSynchronizer {
  
  /**
   * the logger for LocalDatabaseSynchronizer
   */
  private static final Logger logger = LoggerFactory.getLogger(LocalDatabaseSynchronizer.class);

  private final AdminDetector adminSelector;
  private final UserManager userManager;
  private final UserDAO userDAO;

  @Inject
  public LocalDatabaseSynchronizer(AdminDetector adminSelector, UserManager userManager, UserDAO userDAO) {
    this.adminSelector = adminSelector;
    this.userManager = userManager;
    this.userDAO = userDAO;
  }
  
  /**
   * Check for disabled and administrator marks on authenticated users and synchronize the state with the local 
   * database.
   * 
   * @param user authenticated user
   * @param groups groups of authenticated user
   */
  public void synchronize(User user, Set<String> groups) {
    adminSelector.checkForAuthenticatedAdmin(user, groups);

    User dbUser = userDAO.get(user.getId());
    if (dbUser != null) {
      synchronizeWithLocalDatabase(user, dbUser);
    } else if (user.isValid()) {
      createUserInLocalDatabase(user);
    } else {
      logger.warn("could not create user {}, beacause it is not valid", user.getName());
    }
  }
  
  private void synchronizeWithLocalDatabase(User user, User dbUser) {
    synchronizeAdminFlag(user, dbUser);
    synchronizeActiveFlag(user, dbUser);
    modifyUserInLocalDatabase(user, dbUser);
  }
  
  private void synchronizeAdminFlag(User user, User dbUser) {
    // if database user is an admin, set admin for the current user
    if (dbUser.isAdmin()) {
      logger.debug("user {} of type {} is marked as admin by local database", user.getName(), user.getType());
      user.setAdmin(true);
    }
  }

  private void synchronizeActiveFlag(User user, User dbUser) {
    // user is deactivated by database
    if (!dbUser.isActive()) {
      logger.debug("user {} is marked as deactivated by local database", user.getName());
      user.setActive(false);
    }
  }
  
  private void createUserInLocalDatabase(User user) {
    user.setCreationDate(System.currentTimeMillis());
    UserEventHack.fireEvent(userManager, user, HandlerEvent.BEFORE_CREATE);
    userDAO.add(user);
    UserEventHack.fireEvent(userManager, user, HandlerEvent.CREATE);
  }
  
  private void modifyUserInLocalDatabase(User user, User dbUser) {
    // modify existing user, copy properties except password and admin
    if (user.copyProperties(dbUser, false)) {
      user.setLastModified(System.currentTimeMillis());
      UserEventHack.fireEvent(userManager, user, HandlerEvent.BEFORE_MODIFY);
      userDAO.modify(user);
      UserEventHack.fireEvent(userManager, user, HandlerEvent.MODIFY);
    }
  }
  
}
