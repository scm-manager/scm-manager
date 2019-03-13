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



package sonia.scm.user;

import com.github.sdorra.ssp.PermissionActionCheck;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.ManagerDaoAdapter;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.TransformFilter;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.util.CollectionAppender;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  /**
   * Constructs ...
   *
   * @param userDAO
   */
  @Inject
  public DefaultUserManager(UserDAO userDAO)
  {
    this.userDAO = userDAO;
    this.managerDaoAdapter = new ManagerDaoAdapter<>(userDAO);
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
    managerDaoAdapter.modify(
      user,
      UserPermissions::modify,
      notModified -> fireEvent(HandlerEventType.BEFORE_MODIFY, user, notModified),
      notModified -> fireEvent(HandlerEventType.MODIFY, user, notModified));
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

  @Override
  public Collection<User> autocomplete(String filter) {
    UserPermissions.autocomplete().check();
    SearchRequest searchRequest = new SearchRequest(filter, true, DEFAULT_LIMIT);
    return SearchUtil.search(searchRequest, userDAO.getAll(), user -> matches(searchRequest,user)?user:null);
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
    return SearchUtil.search(searchRequest, userDAO.getAll(), new TransformFilter<User>() {
      @Override
      public User accept(User user)
      {
        User result = null;
        if (check.isPermitted(user) && matches(searchRequest, user)) {
          result = user.clone();
        }
        return result;
      }
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
    return getAll(null);
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
  public Collection<User> getAll(Comparator<User> comparator)
  {
    List<User> users = new ArrayList<>();

    PermissionActionCheck<User> check = UserPermissions.read();
    for (User user : userDAO.getAll()) {
      if (check.isPermitted(user)) {
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
      new CollectionAppender<User>()
    {
      @Override
      public void append(Collection<User> collection, User item)
      {
        if (check.isPermitted(item)) {
          collection.add(item.clone());
        }
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

    if (!user.getPassword().equals(oldPassword)) {
      throw new InvalidPasswordException(ContextEntry.ContextBuilder.entity("PasswordChange", "-").in(User.class, user.getName()));
    }

    user.setPassword(newPassword);

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
    if (!isTypeDefault(user)) {
      throw new ChangePasswordNotAllowedException(ContextEntry.ContextBuilder.entity("PasswordChange", "-").in(User.class, user.getName()), user.getType());
    }
    user.setPassword(newPassword);
    this.modify(user);
  }

  /**
   * Method description
   *
   *
   * @param unmarshaller
   * @param path
   */
  private void createDefaultAccount(Unmarshaller unmarshaller, String path)
  {
    InputStream input = DefaultUserManager.class.getResourceAsStream(path);

    try
    {
      User user = (User) unmarshaller.unmarshal(input);

      user.setType(userDAO.getType());
      user.setCreationDate(System.currentTimeMillis());
      userDAO.add(user);
    }
    catch (Exception ex)
    {
      logger.error("could not create account", ex);
    }
    finally
    {
      IOUtil.close(input);
    }
  }

  //~--- fields ---------------------------------------------------------------

  private final UserDAO userDAO;
  private final ManagerDaoAdapter<User> managerDaoAdapter;
}
