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

import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NotFoundException;
import sonia.scm.group.ExternalGroupNames;
import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupManager;
import sonia.scm.plugin.Extension;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;

/**
 * Helper class for syncing realms. The class should simplify the creation of realms, which are syncing authenticated
 * users with the local database.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public final class SyncingRealmHelper {

  private static final Logger LOG = LoggerFactory.getLogger(SyncingRealmHelper.class);

  private final AdministrationContext ctx;
  private final UserManager userManager;
  private final GroupManager groupManager;
  private final GroupCollector groupCollector;

  /**
   * Constructs a new SyncingRealmHelper.
   *
   * @param ctx administration context
   * @param userManager user manager
   * @param groupManager group manager
   * @param groupDAO group dao
   */
  @Inject
  public SyncingRealmHelper(AdministrationContext ctx, UserManager userManager, GroupManager groupManager, GroupDAO groupDAO) {
    this.ctx = ctx;
    this.userManager = userManager;
    this.groupManager = groupManager;
    this.groupCollector = new GroupCollector(groupDAO);
  }

  /**
   * Create {@link AuthenticationInfo} from user and groups.
   */
  public AuthenticationInfoBuilder.ForRealm authenticationInfo() {
    return new AuthenticationInfoBuilder().new ForRealm();
  }

  public class AuthenticationInfoBuilder {
    private String realm;
    private User user;
    private Collection<String> groups = Collections.emptySet();
    private Collection<String> externalGroups = Collections.emptySet();

    private AuthenticationInfo build() {
      return SyncingRealmHelper.this.createAuthenticationInfo(realm, user, groups, externalGroups);
    }

    public class ForRealm {
      private ForRealm() {
      }

      /**
       * Sets the realm.
       * @param realm name of the realm
       */
      public ForUser forRealm(String realm) {
        AuthenticationInfoBuilder.this.realm = realm;
        return AuthenticationInfoBuilder.this.new ForUser();
      }
    }

    public class ForUser {
      private ForUser() {
      }

      /**
       * Sets the user.
       * @param user authenticated user
       */
      public AuthenticationInfoBuilder.WithGroups andUser(User user) {
        AuthenticationInfoBuilder.this.user = user;
        return AuthenticationInfoBuilder.this.new WithGroups();
      }
    }

    public class WithGroups {
      private WithGroups() {
      }

      /**
       * Set the internal groups for the user.
       * @param groups groups of the authenticated user
       * @return builder step for groups
       */
      public WithGroups withGroups(String... groups) {
        return withGroups(asList(groups));
      }

      /**
       * Set the internal groups for the user.
       * @param groups groups of the authenticated user
       * @return builder step for groups
       */
      public WithGroups withGroups(Collection<String> groups) {
        AuthenticationInfoBuilder.this.groups = groups;
        return this;
      }

      /**
       * Set the external groups for the user.
       * @param externalGroups external groups of the authenticated user
       * @return builder step for groups
       */
      public WithGroups withExternalGroups(String... externalGroups) {
        return withExternalGroups(asList(externalGroups));
      }

      /**
       * Set the external groups for the user.
       * @param externalGroups external groups of the authenticated user
       * @return builder step for groups
       */
      public WithGroups withExternalGroups(Collection<String> externalGroups) {
        AuthenticationInfoBuilder.this.externalGroups = externalGroups;
        return this;
      }

      /**
       * Builds the {@link AuthenticationInfo} from the given options.
       *
       * @return complete autentication info
       */
      public AuthenticationInfo build() {
        return AuthenticationInfoBuilder.this.build();
      }
    }
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
  private AuthenticationInfo createAuthenticationInfo(String realm, User user,
    Collection<String> groups, Collection<String> externalGroups) {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    collection.add(user.getId(), realm);
    collection.add(user, realm);
    collection.add(groupCollector.collect(user.getId(), groups), realm);
    collection.add(new ExternalGroupNames(externalGroups), realm);

    return new SimpleAuthenticationInfo(collection, user.getPassword());
  }

  /**
   * Stores the group in local database of scm-manager.
   *
   * @param group group to store
   */
  public void store(final Group group) {
    ctx.runAsAdmin(() -> {
      if (groupManager.get(group.getId()) != null) {
        try {
          groupManager.modify(group);
        } catch (NotFoundException e) {
          throw new IllegalStateException("got NotFoundException though group " + group.getName() + " could be loaded", e);
        }
      } else {
        try {
          groupManager.create(group);
        } catch (AlreadyExistsException e) {
          throw new IllegalStateException("got AlreadyExistsException though group " + group.getName() + " could not be loaded", e);
        }
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
      if (userManager.contains(user.getName())) {
        try {
          userManager.modify(user);
        } catch (NotFoundException e) {
          throw new IllegalStateException("got NotFoundException though user " + user.getName() + " could be loaded", e);
        }
      } else {
        try {
          userManager.create(user);
        } catch (AlreadyExistsException e) {
          throw new IllegalStateException("got AlreadyExistsException though user " + user.getName() + " could not be loaded", e);

        }
      }
    });
  }
}
