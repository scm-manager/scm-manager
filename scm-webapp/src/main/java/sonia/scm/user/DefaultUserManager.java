/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton @EagerSingleton
public class DefaultUserManager extends AbstractUserManager
{

  /** Field description */
  public static final String STORE_NAME = "users";

  /** the logger for XmlUserManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultUserManager.class);

  //~--- constructors ---------------------------------------------------------

  private final PasswordService passwordService;

  /**
   * Constructs ...
   *
   * @param passwordService
   * @param userDAO
   */
  @Inject
  public DefaultUserManager(PasswordService passwordService, UserDAO userDAO, Set<Auditor> auditors)
  {
    this.passwordService = passwordService;
    this.userDAO = userDAO;
    this.managerDaoAdapter = new ManagerDaoAdapter<>(userDAO, auditors);
  }

  //~--- methods --------------------------------------------------------------

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
   * @param username
   *
   * @return
   */
  @Override
  public boolean contains(String username)
  {
    return userDAO.contains(username);
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   */
  @Override
  public User create(User user) {
    String type = user.getType();
    if (Util.isEmpty(type)) {
      user.setType(userDAO.getType());
    }

    logger.info("create user {} of type {}", user.getName(), user.getType());
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
    logger.info("delete user {} of type {}", user.getName(), user.getType());
    managerDaoAdapter.delete(
      user,
      () -> UserPermissions.delete(user.getName()),
      toDelete -> fireEvent(HandlerEventType.BEFORE_DELETE, toDelete),
      toDelete -> fireEvent(HandlerEventType.DELETE, toDelete)
    );
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
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   */
  @Override
  public void modify(User user) {
    logger.info("modify user {} of type {}", user.getName(), user.getType());
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

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   */
  @Override
  public void refresh(User user) {
    if (logger.isInfoEnabled())
    {
      logger.info("refresh user {} of type {}", user.getName(), user.getType());
    }

    UserPermissions.read(user).check();
    User fresh = userDAO.get(user.getName());

    if (fresh == null)
    {
      throw new NotFoundException(User.class, user.getName());
    }

    fresh.copyProperties(user);
  }

  /**
   * Method description
   *
   *
   * @param searchRequest
   *
   * @return
   */
  @Override
  public Collection<User> search(final SearchRequest searchRequest)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("search user with query {}", searchRequest.getQuery());
    }

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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public User get(String id)
  {
    UserPermissions.read().check(id);

    User user = userDAO.get(id);

    if (user != null)
    {
      user = user.clone();
    }

    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<User> getAll()
  {
    return getAll(user -> true, null);
  }

  /**
   * Method description
   *
   *
   * @param comparator
   *
   * @return
   */
  @Override
  public Collection<User> getAll(Predicate<User> filter, Comparator<User> comparator)
  {
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

  /**
   * Method description
   *
   *
   *
   * @param comaparator
   * @param start
   * @param limit
   *
   * @return
   */
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

  /**
   * Method description
   *
   *
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<User> getAll(int start, int limit)
  {
    return getAll(null, start, limit);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getDefaultType()
  {
    return userDAO.getType();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Long getLastModified()
  {
    return userDAO.getLastModified();
  }

  //~--- methods --------------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  private final UserDAO userDAO;
  private final ManagerDaoAdapter<User> managerDaoAdapter;
}
