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



package sonia.scm.user.xml;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.security.SecurityContext;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.AbstractUserManager;
import sonia.scm.user.User;
import sonia.scm.user.UserAllreadyExistException;
import sonia.scm.user.UserException;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SecurityUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlUserManager extends AbstractUserManager
{

  /** Field description */
  public static final String ADMIN_PATH = "/sonia/scm/config/admin-account.xml";

  /** Field description */
  public static final String ANONYMOUS_PATH =
    "/sonia/scm/config/anonymous-account.xml";

  /** Field description */
  public static final String STORE_NAME = "users";

  /** Field description */
  public static final String TYPE = "xml";

  /** the logger for XmlUserManager */
  private static final Logger logger =
    LoggerFactory.getLogger(XmlUserManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param scurityContextProvider
   * @param storeFactory
   */
  @Inject
  public XmlUserManager(Provider<SecurityContext> scurityContextProvider,
                        StoreFactory storeFactory)
  {
    this.scurityContextProvider = scurityContextProvider;
    this.store = storeFactory.getStore(XmlUserDatabase.class, STORE_NAME);
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
    return userDB.contains(username);
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
    if (logger.isInfoEnabled())
    {
      logger.info("create user {} of type {}", user.getName(), user.getType());
    }

    User currentUser = SecurityUtil.getCurrentUser(scurityContextProvider);

    if (!user.equals(currentUser) &&!currentUser.isAdmin())
    {
      throw new ScmSecurityException("admin account is required");
    }

    if (userDB.contains(user.getName()))
    {
      throw new UserAllreadyExistException();
    }

    String type = user.getType();

    if (Util.isEmpty(type))
    {
      user.setType(TYPE);
    }

    user.setCreationDate(System.currentTimeMillis());

    synchronized (XmlUserManager.class)
    {
      userDB.add(user.clone());
      storeDB();
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
  public void delete(User user) throws UserException, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("delete user {} of type {}", user.getName(), user.getType());
    }

    SecurityUtil.assertIsAdmin(scurityContextProvider);

    String name = user.getName();

    if (userDB.contains(name))
    {
      synchronized (XmlUserManager.class)
      {
        userDB.remove(name);
        storeDB();
      }
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
    userDB = store.get();

    if (userDB == null)
    {
      userDB = new XmlUserDatabase();
      userDB.setCreationTime(System.currentTimeMillis());
      createDefaultAccounts();
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

    if (userDB.contains(name))
    {
      synchronized (XmlUserManager.class)
      {
        userDB.remove(name);
        userDB.add(user.clone());
        storeDB();
      }
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

    User fresh = userDB.get(user.getName());

    if (fresh == null)
    {
      throw new UserException("user does not exists");
    }

    fresh.copyProperties(user);
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
    User user = userDB.get(id);

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
    SecurityUtil.assertIsAdmin(scurityContextProvider);

    LinkedList<User> users = new LinkedList<User>();

    for (User user : userDB.values())
    {
      users.add(user.clone());
    }

    return users;
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
    InputStream input = XmlUserManager.class.getResourceAsStream(path);

    try
    {
      User user = (User) unmarshaller.unmarshal(input);

      userDB.add(user);
      storeDB();
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

  /**
   * Method description
   *
   */
  private void storeDB()
  {
    userDB.setLastModified(System.currentTimeMillis());
    store.set(userDB);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<SecurityContext> scurityContextProvider;

  /** Field description */
  private Store<XmlUserDatabase> store;

  /** Field description */
  private XmlUserDatabase userDB;
}
