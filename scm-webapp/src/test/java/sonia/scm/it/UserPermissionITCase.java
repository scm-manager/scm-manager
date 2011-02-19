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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.Util;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class UserPermissionITCase
{

  /**
   * Constructs ...
   *
   *
   *
   * @param credentials
   */
  public UserPermissionITCase(Credentials credentials)
  {
    this.credentials = credentials;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Parameters
  public static Collection<Credentials[]> createParameters()
  {
    Collection<Credentials[]> params = new ArrayList<Credentials[]>();

    params.add(new Credentials[] { new Credentials() });

    User dent = createTestUser();

    params.add(new Credentials[] {
      new Credentials(dent.getName(), dent.getPassword()) });

    return params;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private static User createTestUser()
  {
    User dent = UserTestData.createDent();

    dent.setPassword("a.dent124");

    Client client = createClient();

    authenticateAdmin(client);

    WebResource wr = createResource(client, "users");
    ClientResponse response = wr.post(ClientResponse.class, dent);

    assertNotNull(response);
    assertTrue(response.getStatus() == 201);
    response.close();
    logoutClient(client);
    client.destroy();

    return dent;
  }

  /**
   * Method description
   *
   */
  @After
  public void after()
  {
    client = createClient();
    logout();
  }

  /**
   * Method description
   *
   */
  @Before
  public void before()
  {
    client = createClient();
    login();
  }

  /**
   * Method description
   *
   */
  @Test
  public void create()
  {
    WebResource wr = createResource(client, "users");
    User zaphod = UserTestData.createZaphod();

    checkResponse(wr.post(ClientResponse.class, zaphod));
  }

  /**
   * Method description
   *
   */
  @Test
  public void delete()
  {
    WebResource wr = createResource(client, "users/scmadmin");

    checkResponse(wr.delete(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void modify()
  {
    WebResource wr = createResource(client, "users/scmadmin");
    User user = new User("scmadmin", "SCM Administrator",
                         "scm-admin@scm-manager.org");

    user.setPassword("hallo123");
    user.setAdmin(true);
    user.setType("xml");
    checkResponse(wr.put(ClientResponse.class, user));
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

    checkResponse(wr.get(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void getAll()
  {
    WebResource wr = createResource(client, "users");

    checkResponse(wr.get(ClientResponse.class));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   */
  private void checkResponse(ClientResponse response)
  {
    assertNotNull(response);
    System.out.append("STATUS: ").println(response.getStatus());

    if (credentials.isAnonymous())
    {
      assertTrue(response.getStatus() == 401);
    }
    else
    {
      assertTrue(response.getStatus() == 403);
    }

    response.close();
  }

  /**
   * Method description
   *
   */
  private void login()
  {
    if (!credentials.isAnonymous())
    {
      authenticate(client, credentials.getUsername(),
                   credentials.getPassword());
    }
  }

  /**
   * Method description
   *
   */
  private void logout()
  {
    if (!credentials.isAnonymous())
    {
      logoutClient(client);
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2011-02-19
   * @author         Sebastian Sdorra
   */
  public static class Credentials
  {

    /**
     * Constructs ...
     *
     */
    public Credentials() {}

    /**
     * Constructs ...
     *
     *
     * @param username
     * @param password
     */
    public Credentials(String username, String password)
    {
      this.password = password;
      this.username = username;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getPassword()
    {
      return password;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getUsername()
    {
      return username;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public boolean isAnonymous()
    {
      return Util.isEmpty(username) && Util.isEmpty(password);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String password;

    /** Field description */
    private String username;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Client client;

  /** Field description */
  private Credentials credentials;
}
