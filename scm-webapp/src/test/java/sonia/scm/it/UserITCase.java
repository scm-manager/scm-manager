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



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.AfterClass;
import org.junit.Test;

import sonia.scm.ScmState;
import sonia.scm.Type;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.Collection;

import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
public class UserITCase extends AbstractAdminITCaseBase
{

  /**
   * Method description
   *
   */
  @AfterClass
  public static void cleanup()
  {
    Client client = createClient();

    authenticateAdmin(client);
    createResource(client, "users/slarti").delete();
    client.destroy();
  }

  /**
   * Method description
   *
   */
  @Test
  public void create()
  {
    User slarti = UserTestData.createSlarti();

    slarti.setPassword("slarti123");
    createUser(slarti);
  }

  /**
   * Method description
   *
   */
  @Test
  public void delete()
  {
    User dent = UserTestData.createDent();

    createUser(dent);
    deleteUser(dent);
  }

  /**
   * Method description
   *
   */
  @Test
  public void modify()
  {
    User marvin = UserTestData.createMarvin();

    createUser(marvin);
    marvin = getUser(marvin.getName());
    marvin.setDisplayName("Paranoid Android");

    WebResource wr = createResource(client, "users/".concat(marvin.getName()));
    ClientResponse response =
      wr.type(MediaType.APPLICATION_XML).put(ClientResponse.class, marvin);

    assertNotNull(response);
    assertEquals(response.getStatus(), 204);
    response.close();

    User other = getUser(marvin.getName());

    assertEquals(marvin.getDisplayName(), other.getDisplayName());
    assertNotNull(other.getLastModified());
    deleteUser(marvin);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void get()
  {
    User scmadmin = getUser("scmadmin");

    testAdmin(scmadmin);
  }

  /**
   * Method description
   *
   */
  @Test
  public void getAll()
  {
    WebResource wr = createResource(client, "users");
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);
    assertEquals(response.getStatus(), 200);

    Collection<User> users =
      response.getEntity(new GenericType<Collection<User>>() {}
    );

    response.close();
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
   * @param client
   */
  protected void adminLogin(Client client)
  {
    ClientResponse cr = authenticate(client, "scmadmin", "scmadmin");
    ScmState state = cr.getEntity(ScmState.class);

    cr.close();
    assertNotNull(state);
    assertTrue(state.isSuccess());

    User user = state.getUser();

    assertNotNull(user);
    assertEquals(user.getName(), "scmadmin");
    assertTrue(user.isAdmin());

    Collection<Type> types = state.getRepositoryTypes();

    assertNotNull(types);
    assertFalse(types.isEmpty());
  }

  /**
   * Method description
   *
   *
   * @param user
   */
  private void createUser(User user)
  {
    WebResource wr = createResource(client, "users");
    ClientResponse response =
      wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, user);

    assertNotNull(response);
    assertEquals(response.getStatus(), 201);
    response.close();

    User other = getUser(user.getName());

    assertEquals(user.getName(), other.getName());
    assertEquals(user.getDisplayName(), other.getDisplayName());
    assertEquals(user.getMail(), other.getMail());
    assertNotNull(other.getType());
    assertNotNull(other.getCreationDate());
  }

  /**
   * Method description
   *
   *
   * @param user
   */
  private void deleteUser(User user)
  {
    WebResource wr = createResource(client, "users/".concat(user.getName()));
    ClientResponse response = wr.delete(ClientResponse.class);

    assertNotNull(response);
    assertEquals(response.getStatus(), 204);
    response.close();
    wr = createResource(client, "users/".concat(user.getName()));
    response = wr.get(ClientResponse.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), 404);
    response.close();
  }

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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   *
   * @return
   */
  private User getUser(String username)
  {
    WebResource wr = createResource(client, "users/".concat(username));
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);
    assertEquals(response.getStatus(), 200);

    User user = response.getEntity(User.class);

    response.close();
    assertNotNull(user);

    return user;
  }
}
