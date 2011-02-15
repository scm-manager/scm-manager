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

import org.junit.Test;

import sonia.scm.ScmState;
import sonia.scm.Type;
import sonia.scm.user.User;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import java.util.Collection;

import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Sebastian Sdorra
 */
public class AuthenticationITCase
{

  /** Field description */
  public static final String BASE_URL =
    "http://localhost:8080/scm-webapp/api/rest/";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   */
  @Test
  public void testLogin()
  {
    Client client = new Client();

    adminLogin(client);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testLoginFailed()
  {
    Client client = new Client();
    ClientResponse response = login(client, "dent", "trillian");

    assertNotNull(response);
    assertTrue(response.getStatus() == 401);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testLogout()
  {
    Client client = new Client();

    adminLogin(client);
    logout(client);
  }

  /**
   * Method description
   *
   *
   * @param client
   */
  private void adminLogin(Client client)
  {
    ClientResponse cr = login(client, "scmadmin", "scmadmin");
    ScmState state = cr.getEntity(ScmState.class);

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
   * @param client
   * @param username
   * @param password
   *
   * @return
   */
  private ClientResponse login(Client client, String username, String password)
  {
    WebResource wr =
      client.resource(BASE_URL.concat("authentication/login.json"));
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();

    formData.add("username", username);
    formData.add("password", password);

    return wr.type("application/x-www-form-urlencoded").post(
        ClientResponse.class, formData);
  }

  /**
   * Method description
   *
   *
   * @param client
   */
  private void logout(Client client)
  {
    WebResource wr =
      client.resource(BASE_URL.concat("authentication/logout.json"));
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);
    assertTrue(response.getStatus() == 200);
  }
}
