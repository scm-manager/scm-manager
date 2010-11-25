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
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.HandlerEvent;
import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class BasicUserManager implements UserManager
{

  /** the logger for BasicUserManager */
  private static final Logger logger =
    LoggerFactory.getLogger(BasicUserManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handlerSet
   */
  @Inject
  public BasicUserManager(Set<UserHandler> handlerSet)
  {
    AssertUtil.assertIsNotEmpty(handlerSet);

    for (UserHandler handler : handlerSet)
    {
      addHandler(handler);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(UserListener listener)
  {
    listenerSet.add(listener);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    for (UserHandler handler : handlerMap.values())
    {
      IOUtil.close(handler);
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws Exception
   * @throws IOException
   */
  @Override
  public void create(User user) throws Exception, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("create user {} of type {}", user.getName(), user.getType());
    }

    AssertUtil.assertIsValid(user);
    getHandler(user).create(user);
    fireEvent(user, HandlerEvent.CREATE);
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws Exception
   * @throws IOException
   */
  @Override
  public void delete(User user) throws Exception, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("delete user {} of type {}", user.getName(), user.getType());
    }

    getHandler(user).delete(user);
    fireEvent(user, HandlerEvent.DELETE);
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
    for (UserHandler handler : handlerMap.values())
    {
      handler.init(context);
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws Exception
   * @throws IOException
   */
  @Override
  public void modify(User user) throws Exception, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("modify user {} of type {}", user.getName(), user.getType());
    }

    AssertUtil.assertIsValid(user);
    getHandler(user).modify(user);
    fireEvent(user, HandlerEvent.MODIFY);
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws Exception
   * @throws IOException
   */
  @Override
  public void refresh(User user) throws Exception, IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("refresh user {} of type {}", user.getName(), user.getType());
    }

    // getHandler(user).refresh(user);
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(UserListener listener)
  {
    listenerSet.remove(listener);
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
    User user = null;

    for (UserHandler handler : handlerMap.values())
    {
      if (handler.isConfigured())
      {

        // user = handler.get(id);
        if (user != null)
        {
          break;
        }
      }
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
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch all users");
    }

    Set<User> repositories = new HashSet<User>();

    /*
     * for (UserHandler handler : handlerMap.values())
     * {
     * if (handler.isConfigured())
     * {
     *   Collection<User> handlerRepositories = handler.getAll();
     *
     *   if (handlerRepositories != null)
     *   {
     *     repositories.addAll(handlerRepositories);
     *   }
     * }
     * }
     */
    if (logger.isDebugEnabled())
    {
      logger.debug("fetched {} users", repositories.size());
    }

    return repositories;
  }

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  @Override
  public UserHandler getHandler(String type)
  {
    return handlerMap.get(type);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Type> getTypes()
  {
    return typeSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handler
   */
  private void addHandler(UserHandler handler)
  {
    AssertUtil.assertIsNotNull(handler);

    Type type = handler.getType();

    AssertUtil.assertIsNotNull(type);

    if (handlerMap.containsKey(type.getName()))
    {
      throw new ConfigurationException(
          type.getName().concat("allready registered"));
    }

    if (logger.isInfoEnabled())
    {
      logger.info("added UserHandler {} for type {}", handler.getClass(), type);
    }

    handlerMap.put(type.getName(), handler);
    typeSet.add(type);
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param event
   */
  private void fireEvent(User user, HandlerEvent event)
  {
    for (UserListener listener : listenerSet)
    {
      listener.onEvent(user, event);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   *
   * @throws UserException
   */
  private UserHandler getHandler(User user) throws UserException
  {
    AssertUtil.assertIsNotNull(user);

    String type = user.getType();

    AssertUtil.assertIsNotEmpty(type);

    UserHandler handler = handlerMap.get(type);

    if (handler == null)
    {
      throw new UserHandlerNotFoundException(
          "could not find UserHandler for ".concat(type));
    }
    else if (!handler.isConfigured())
    {
      throw new UserException(
          "UserHandler for type ".concat(type).concat(" is not configured"));
    }

    return handler;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<UserListener> listenerSet = new HashSet<UserListener>();

  /** Field description */
  private Map<String, UserHandler> handlerMap = new HashMap<String,
                                                  UserHandler>();

  /** Field description */
  private Set<Type> typeSet = new LinkedHashSet<Type>();
}
