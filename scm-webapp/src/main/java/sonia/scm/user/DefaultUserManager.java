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

package sonia.scm.user;

import com.github.sdorra.ssp.PermissionActionCheck;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.ManagerDaoAdapter;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.auditlog.Auditor;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.security.Authentications;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


@Singleton
@EagerSingleton
public class DefaultUserManager extends AbstractUserManager {

  public static final String STORE_NAME = "users";
  private static final Logger LOG = LoggerFactory.getLogger(DefaultUserManager.class);

  private final UserDAO userDAO;
  private final ManagerDaoAdapter<User> managerDaoAdapter;
  private final PasswordService passwordService;

  @Inject
  public DefaultUserManager(PasswordService passwordService, UserDAO userDAO, Set<Auditor> auditors) {
    this.passwordService = passwordService;
    this.userDAO = userDAO;
    this.managerDaoAdapter = new ManagerDaoAdapter<>(userDAO, auditors);
  }

  @Override
  public void close() throws IOException {
    // do nothing
  }

  @Override
  public boolean contains(String username) {
    return userDAO.contains(username);
  }

  @Override
  public User create(User user) {
    String type = user.getType();
    if (Util.isEmpty(type)) {
      user.setType(userDAO.getType());
    }
    LOG.info("create user {}", user.getName());
    ensurePasswordEncrypted(user);

    return managerDaoAdapter.create(
      user,
      UserPermissions::create,
      newUser -> fireEvent(HandlerEventType.BEFORE_CREATE, newUser),
      newUser -> fireEvent(HandlerEventType.CREATE, newUser)
    );
  }

  @Override
  public void delete(User user) {
    LOG.info("delete user {} of type {}", user.getName());
    managerDaoAdapter.delete(
      user,
      () -> UserPermissions.delete(user.getName()),
      toDelete -> fireEvent(HandlerEventType.BEFORE_DELETE, toDelete),
      toDelete -> fireEvent(HandlerEventType.DELETE, toDelete)
    );
  }

  @Override
  public void init(SCMContextProvider context) {
  }

  @Override
  public void modify(User user) {
    LOG.info("modify user {}", user.getName());
    ensurePasswordEncrypted(user);
    managerDaoAdapter.modify(
      user,
      UserPermissions::modify,
      notModified -> fireEvent(HandlerEventType.BEFORE_MODIFY, user, notModified),
      notModified -> fireEvent(HandlerEventType.MODIFY, user, notModified));
  }

  private void ensurePasswordEncrypted(User user) {
    user.setPassword(passwordService.encryptPassword(user.getPassword()));
  }

  @Override
  public void refresh(User user) {
    LOG.info("refresh user {}", user.getName());

    UserPermissions.read(user).check();
    User fresh = userDAO.get(user.getName());

    if (fresh == null) {
      throw new NotFoundException(User.class, user.getName());
    }

    fresh.copyProperties(user);
  }

  @Override
  public Collection<User> search(final SearchRequest searchRequest) {
    LOG.debug("search user with query {}", searchRequest.getQuery());

    final PermissionActionCheck<User> check = UserPermissions.read();
    return SearchUtil.search(searchRequest, userDAO.getAll(), user -> {
      if (check.isPermitted(user) && matches(searchRequest, user)) {
        return user.clone();
      }
      return null;
    });
  }

  private boolean matches(SearchRequest searchRequest, User user) {
    return SearchUtil.matchesOne(searchRequest, user.getName(), user.getDisplayName(), user.getMail());
  }

  @Override
  public User get(String id) {
    UserPermissions.read().check(id);

    User user = userDAO.get(id);

    if (user != null) {
      user = user.clone();
    }

    return user;
  }

  @Override
  public Collection<User> getAll() {
    return getAll(user -> true, null);
  }

  @Override
  public Collection<User> getAll(Predicate<User> filter, Comparator<User> comparator) {
    List<User> users = new ArrayList<>();

    PermissionActionCheck<User> check = UserPermissions.read();
    for (User user : userDAO.getAll()) {
      if (filter.test(user) && check.isPermitted(user)) {
        users.add(user.clone());
      }
    }

    if (comparator != null) {
      Collections.sort(users, comparator);
    }

    return users;
  }

  @Override
  public Collection<User> getAll(Comparator<User> comaparator, int start, int limit) {
    final PermissionActionCheck<User> check = UserPermissions.read();
    return Util.createSubCollection(userDAO.getAll(), comaparator,
      (collection, item) -> {
        if (check.isPermitted(item)) {
          collection.add(item.clone());
        }
      }, start, limit);
  }

  @Override
  public Collection<User> getAll(int start, int limit) {
    return getAll(null, start, limit);
  }

  @Override
  public String getDefaultType() {
    return userDAO.getType();
  }

  @Override
  public Long getLastModified() {
    return userDAO.getLastModified();
  }


  @Override
  public void changePasswordForLoggedInUser(String oldPassword, String newPassword) {
    User user = get((String) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal());

    if (!isAnonymousUser(user) && !user.getPassword().equals(oldPassword)) {
      throw new InvalidPasswordException(ContextEntry.ContextBuilder.entity("PasswordChange", "-").in(User.class, user.getName()));
    }

    user.setPassword(passwordService.encryptPassword(newPassword));

    managerDaoAdapter.modify(
      user,
      UserPermissions::changePassword,
      notModified -> fireEvent(HandlerEventType.BEFORE_MODIFY, user, notModified),
      notModified -> fireEvent(HandlerEventType.MODIFY, user, notModified));
  }

  @Override
  public void overwritePassword(String userId, String newPassword) {
    User user = get(userId);
    if (user == null) {
      throw new NotFoundException(User.class, userId);
    }
    if (isAnonymousUser(user) || user.isExternal()) {
      throw new ChangePasswordNotAllowedException(ContextEntry.ContextBuilder.entity("PasswordChange", "-").in(User.class, user.getName()), "external");
    }
    user.setPassword(newPassword);
    this.modify(user);
  }

  private boolean isAnonymousUser(User user) {
    return Authentications.isSubjectAnonymous(user.getName());
  }

}
