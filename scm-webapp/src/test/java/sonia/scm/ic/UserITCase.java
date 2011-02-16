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



package sonia.scm.ic;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sonia.scm.user.User;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class UserITCase extends AbstractITCaseBase
{

  /**
   * Method description
   *
   */
  @Before
  public void login()
  {
    client = createClient();
    adminLogin(client);
  }

  /**
   * Method description
   *
   */
  @After
  public void logout()
  {
    logout(client);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void get()
  {
    WebResource wr = createResource(client, "users/scmadmin");
    ClientResponse respone = wr.get(ClientResponse.class);

    assertNotNull(respone);
    assertTrue(respone.getStatus() == 200);

    User user = respone.getEntity(User.class);

    testAdmin(user);
  }

  /**
   * Method description
   * 
   */
  @Test
  public void getAll()
  {
    WebResource wr = createResource(client, "users");
    ClientResponse respone = wr.get(ClientResponse.class);

    assertNotNull(respone);
    assertTrue(respone.getStatus() == 200);

    Collection<User> users =
      respone.getEntity(new GenericType<Collection<User>>() {}
    );

    assertNotNull(users);
    assertFalse(users.isEmpty());

    User admin = null;

    for (User user : users)
    {
      if (user.getName().equals("scmadmin"))
      {
        admin = user;
      }
    }

    testAdmin(admin);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   */
  private void testAdmin(User user)
  {
    assertNotNull(user);
    assertEquals(user.getName(), "scmadmin");
    assertTrue(user.isAdmin());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Client client;
}
