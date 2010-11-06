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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("users")
@Singleton
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class UserResource extends AbstractResource<User>
{

  /** Field description */
  public static final String DUMMY_PASSWORT = "__dummypassword__";

  /** Field description */
  public static final String PATH_PART = "users";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param userManager
   */
  @Inject
  public UserResource(UserManager userManager)
  {
    this.userManager = userManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws Exception
   */
  @Override
  protected void addItem(User user) throws Exception
  {
    userManager.create(user);
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws Exception
   */
  @Override
  protected void removeItem(User user) throws Exception
  {
    userManager.delete(user);
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param user
   *
   * @throws Exception
   */
  @Override
  protected void updateItem(String name, User user) throws Exception
  {
    userManager.modify(user);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected User[] getAllItems()
  {
    User[] users = null;
    Collection<User> userCollection = userManager.getAll();

    if (Util.isNotEmpty(userCollection))
    {
      int size = userCollection.size();

      users = new User[size];

      int i = 0;

      for (User u : userCollection)
      {
        u.setPassword(DUMMY_PASSWORT);
        users[i] = u;
        i++;
      }
    }

    return users;
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  @Override
  protected String getId(User user)
  {
    return user.getName();
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  protected User getItem(String name)
  {
    User user = userManager.get(name);

    if (user != null)
    {
      user.setPassword(DUMMY_PASSWORT);
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
  protected String getPathPart()
  {
    return PATH_PART;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private UserManager userManager;
}
