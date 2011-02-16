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

import sonia.scm.security.EncryptionHandler;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("users")
@Singleton
public class UserResource extends AbstractManagerResource<User, UserException>
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
   * @param encryptionHandler
   */
  @Inject
  public UserResource(UserManager userManager,
                      EncryptionHandler encryptionHandler)
  {
    super(userManager);
    this.encryptionHandler = encryptionHandler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param items
   *
   * @return
   */
  @Override
  protected GenericEntity<Collection<User>> createGenericEntity(
          Collection<User> items)
  {
    return new GenericEntity<Collection<User>>(items) {}
    ;
  }

  /**
   * Method description
   *
   *
   * @param user
   */
  @Override
  protected void preCreate(User user)
  {
    encryptPassword(user);
  }

  /**
   * Method description
   *
   *
   * @param user
   */
  @Override
  protected void preUpate(User user)
  {
    if (DUMMY_PASSWORT.equals(user.getPassword()))
    {
      User o = manager.get(user.getName());

      AssertUtil.assertIsNotNull(o);
      user.setPassword(o.getPassword());
    }
    else
    {
      encryptPassword(user);
    }
  }

  /**
   * Method description
   *
   *
   * @param users
   *
   * @return
   */
  @Override
  protected Collection<User> prepareForReturn(Collection<User> users)
  {
    if (Util.isNotEmpty(users))
    {
      for (User u : users)
      {
        u.setPassword(DUMMY_PASSWORT);
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
  protected User prepareForReturn(User user)
  {
    user.setPassword(DUMMY_PASSWORT);

    return user;
  }

  //~--- get methods ----------------------------------------------------------

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
   * @return
   */
  @Override
  protected String getPathPart()
  {
    return PATH_PART;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   */
  private void encryptPassword(User user)
  {
    String password = user.getPassword();

    if (Util.isNotEmpty(password))
    {
      user.setPassword(encryptionHandler.encrypt(password));
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private EncryptionHandler encryptionHandler;
}
