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

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEvent;
import sonia.scm.SCMContextProvider;
import sonia.scm.TransformFilter;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.CollectionAppender;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SecurityUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultUserManager extends AbstractUserManager
{

  /** Field description */
  public static final String ADMIN_PATH = "/sonia/scm/config/admin-account.xml";

  /** Field description */
  public static final String ANONYMOUS_PATH =
    "/sonia/scm/config/anonymous-account.xml";

  /** Field description */
  public static final String STORE_NAME = "users";

  /** the logger for XmlUserManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultUserManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param scurityContextProvider
   * @param userDAO
   * @param userListenerProvider
   */
  @Inject
  public DefaultUserManager(
          Provider<WebSecurityContext> scurityContextProvider, UserDAO userDAO,
          Provider<Set<UserListener>> userListenerProvider)
  {
    this.scurityContextProvider = scurityContextProvider;
    this.userDAO = userDAO;
    this.userListenerProvider = userListenerProvider;
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
   * @throws UserException
   */
  @Override
  public void create(User user) throws UserException, IOException
  {
    String type = user.getType();

    if (Util.isEmpty(type))
    {
      user.setType(userDAO.getType());
    }

    if (logger.isInfoEnabled())
    {
      logger.info("create user {} of type {}", user.getName(), user.getType());
    }

    User currentUser = SecurityUtil.getCurrentUser(scurityContextProvider);

    if (!user.equals(currentUser) &&!currentUser.isAdmin())
    {
      throw new ScmSecurityException("admin account is required");
    }

    if (userDAO.contains(user.getName()))
    {
      throw new UserAllreadyExistException(user.getName());
    }

    AssertUtil.assertIsValid(user);
    user.setCreationDate(System.currentTimeMillis());
    userDAO.add(user);
    fireEvent(user, HandlerEvent.CREATE);
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   * @throws UserException
   */
  @Override
  public void delete(User user) throws UserException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("delete user {} of type {}", user.getName(), user.getType());
    }

    SecurityUtil.assertIsAdmin(scurityContextProvider);

    String name = user.getName();

    if (userDAO.contains(name))
    {
      userDAO.delete(user);
      fireEvent(user, HandlerEvent.DELETE);
    }
    else
    {
      throw new UserException("user does not exists");
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

    // TODO improve
    if (!userDAO.contains("scmadmin") &&!userDAO.contains("anonymous"))
    {
      createDefaultAccounts();
    }

    Set<UserListener> listeners = userListenerProvider.get();

    if (Util.isNotEmpty(listeners))
    {
      addListeners(listeners);
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   * @throws UserException
   */
  @Override
  public void modify(User user) throws UserException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("modify user {} of type {}", user.getName(), user.getType());
    }

    User currentUser = SecurityUtil.getCurrentUser(scurityContextProvider);

    if (!user.getName().equals(currentUser.getName()) &&!currentUser.isAdmin())
    {
      throw new ScmSecurityException("admin account is required");
    }

    String name = user.getName();

    if (userDAO.contains(name))
    {
      AssertUtil.assertIsValid(user);
      user.setLastModified(System.currentTimeMillis());
      userDAO.modify(user);
      fireEvent(user, HandlerEvent.MODIFY);
    }
    else
    {
      throw new UserException("user does not exists");
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   * @throws UserException
   */
  @Override
  public void refresh(User user) throws UserException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("refresh user {} of type {}", user.getName(), user.getType());
    }

    SecurityUtil.assertIsAdmin(scurityContextProvider);

    User fresh = userDAO.get(user.getName());

    if (fresh == null)
    {
      throw new UserException("user does not exists");
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

    return SearchUtil.search(searchRequest, userDAO.getAll(),
                             new TransformFilter<User>()
    {
      @Override
      public User accept(User user)
      {
        User result = null;

        if (SearchUtil.matchesOne(searchRequest, user.getName(),
                                  user.getDisplayName(), user.getMail()))
        {
          result = user.clone();
        }

        return result;
      }
    });
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

    // SecurityUtil.assertIsAdmin(scurityContextProvider);
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
    SecurityUtil.assertIsAdmin(scurityContextProvider);

    List<User> users = new ArrayList<User>();

    for (User user : userDAO.getAll())
    {
      users.add(user.clone());
    }

    if (comparator != null)
    {
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
  public Collection<User> getAll(Comparator<User> comaparator, int start,
                                 int limit)
  {
    SecurityUtil.assertIsAdmin(scurityContextProvider);

    return Util.createSubCollection(userDAO.getAll(), comaparator,
                                    new CollectionAppender<User>()
    {
      @Override
      public void append(Collection<User> collection, User item)
      {
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

  /**
   * Method description
   *
   */
  private void createDefaultAccounts()
  {
    try
    {
      JAXBContext context = JAXBContext.newInstance(User.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      createDefaultAccount(unmarshaller, ADMIN_PATH);
      createDefaultAccount(unmarshaller, ANONYMOUS_PATH);
    }
    catch (JAXBException ex)
    {
      logger.error(ex.getMessage(), ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<WebSecurityContext> scurityContextProvider;

  /** Field description */
  private UserDAO userDAO;

  /** Field description */
  private Provider<Set<UserListener>> userListenerProvider;
}
