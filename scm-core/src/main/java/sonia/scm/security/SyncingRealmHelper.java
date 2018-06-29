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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.SimplePrincipalCollection;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupNames;
import sonia.scm.plugin.Extension;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;

import java.util.Collection;

/**
 * Helper class for syncing realms. The class should simplify the creation of realms, which are syncing authenticated
 * users with the local database.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public final class SyncingRealmHelper {

  private final AdministrationContext ctx;
  
  private final GroupManager groupManager;

  private final UserManager userManager;
  
  /**
   * Constructs a new SyncingRealmHelper.
   *
   *
   * @param ctx administration context
   * @param userManager user manager
   * @param groupManager group manager
   */
  @Inject
  public SyncingRealmHelper(AdministrationContext ctx, UserManager userManager, GroupManager groupManager) {
    this.ctx = ctx;
    this.userManager = userManager;
    this.groupManager = groupManager;
  }

  //~--- methods --------------------------------------------------------------
  /**
   * Create {@link AuthenticationInfo} from user and groups.
   *
   *
   * @param realm name of the realm
   * @param user authenticated user
   * @param groups groups of the authenticated user
   *
   * @return authentication info
   */
  public AuthenticationInfo createAuthenticationInfo(String realm, User user,
    String... groups) {
    return createAuthenticationInfo(realm, user, ImmutableList.copyOf(groups));
  }

  /**
   * Create {@link AuthenticationInfo} from user and groups.
   *
   *
   * @param realm name of the realm
   * @param user authenticated user
   * @param groups groups of the authenticated user
   *
   * @return authentication info
   */
  public AuthenticationInfo createAuthenticationInfo(String realm, User user,
    Collection<String> groups) {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    collection.add(user.getId(), realm);
    collection.add(user, realm);
    collection.add(new GroupNames(groups), realm);

    return new SimpleAuthenticationInfo(collection, user.getPassword());
  }

  /**
   * Stores the group in local database of scm-manager.
   *
   * @param group group to store
   */
  public void store(final Group group) {
    ctx.runAsAdmin(() -> {
      try {
        if (groupManager.get(group.getId()) != null) {
          groupManager.modify(group);
        }
        else {
          groupManager.create(group);
        }
      }
      catch (GroupException ex) {
        throw new AuthenticationException("could not store group", ex);
      }
    });
  }

  /**
   * Stores the user in local database of scm-manager.
   *
   * @param user user to store
   */
  public void store(final User user) {
    ctx.runAsAdmin(() -> {
      try {
        if (userManager.contains(user.getName())) {
          userManager.modify(user);
        }
        else {
          userManager.create(user);
        }
      }
      catch (UserException ex) {
        throw new AuthenticationException("could not store user", ex);
      }
    });
  }
}
