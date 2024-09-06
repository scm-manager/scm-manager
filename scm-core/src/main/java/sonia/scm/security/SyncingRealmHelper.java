/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.security;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.SimplePrincipalCollection;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NotFoundException;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.plugin.Extension;
import sonia.scm.user.ExternalUserConverter;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;

import java.util.Collections;
import java.util.Set;

/**
 * Helper class for syncing realms. The class should simplify the creation of realms, which are syncing authenticated
 * users with the local database.
 *
 * @since 2.0.0
 */
@Slf4j
@Extension
public final class SyncingRealmHelper {

  private final AdministrationContext ctx;
  private final UserManager userManager;
  private final GroupManager groupManager;
  private final Set<ExternalUserConverter> externalUserConverters;

  /**
   * Constructs a new SyncingRealmHelper.
   *
   * @param ctx                    administration context
   * @param userManager            user manager
   * @param groupManager           group manager
   * @param externalUserConverters global scm configuration
   */
  @Inject
  public SyncingRealmHelper(AdministrationContext ctx, UserManager userManager, GroupManager groupManager, Set<ExternalUserConverter> externalUserConverters) {
    this.ctx = ctx;
    this.userManager = userManager;
    this.groupManager = groupManager;
    this.externalUserConverters = externalUserConverters;
  }

  /**
   * Constructs a new SyncingRealmHelper.
   *
   * @param ctx          administration context
   * @param userManager  user manager
   * @param groupManager group manager
   * @deprecated Use {@link #SyncingRealmHelper(AdministrationContext, UserManager, GroupManager, Set)} instead.
   */
  @Deprecated
  public SyncingRealmHelper(AdministrationContext ctx, UserManager userManager, GroupManager groupManager) {
    this(ctx, userManager, groupManager, Collections.emptySet());
  }

  /**
   * Create {@link AuthenticationInfo} from user and groups.
   *
   * @param realm name of the realm
   * @param user  authenticated user
   * @return authentication info
   */
  public AuthenticationInfo createAuthenticationInfo(String realm, User user) {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    collection.add(user.getId(), realm);
    collection.add(user, realm);

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
        modifyGroup(group);
      } else {
        createNewGroup(group);
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
        modifyUser(user);
      } else {
        createNewUser(user);
      }
    });
  }

  private void createNewUser(User user) {
    try {
      User clone = user.clone();
      // New user created by syncing realm helper is always external
      clone.setExternal(true);
      userManager.create(clone);
    } catch (AlreadyExistsException e) {
      throw new IllegalStateException("got AlreadyExistsException though user " + user.getName() + " could not be loaded", e);

    }
  }

  private void modifyUser(User user) {
    User clone = user.clone();
    if (!externalUserConverters.isEmpty()) {
      log.debug("execute available user converters");
      for (ExternalUserConverter converter : externalUserConverters) {
        clone = converter.convert(clone);
      }
    }

    try {
      userManager.modify(clone);
    } catch (NotFoundException e) {
      throw new IllegalStateException("got NotFoundException though user " + clone.getName() + " could be loaded", e);
    }
  }

  private void createNewGroup(Group group) {
    try {
      groupManager.create(group);
    } catch (AlreadyExistsException e) {
      throw new IllegalStateException("got AlreadyExistsException though group " + group.getName() + " could not be loaded", e);
    }
  }

  private void modifyGroup(Group group) {
    try {
      groupManager.modify(group);
    } catch (NotFoundException e) {
      throw new IllegalStateException("got NotFoundException though group " + group.getName() + " could be loaded", e);
    }
  }
}
