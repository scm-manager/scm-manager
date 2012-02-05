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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.group.Group;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class AdminPermissionITCase
{

  /**
   * Constructs ...
   *
   *
   * @param credentials
   */
  public AdminPermissionITCase(Credentials credentials)
  {
    this.credentials = credentials;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @AfterClass
  public static void cleanup()
  {
    Client client = createAdminClient();

    createResource(client, "users/marvin").delete();
    createResource(client, "groups/test-group").delete();
    client.destroy();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Parameters
  public static Collection<Object[]> createParameters()
  {
    Collection<Object[]> params = new ArrayList<Object[]>();

    params.add(new Object[] { new Credentials() });

    User u = UserTestData.createMarvin();

    u.setPassword("secret");

    Client client = createAdminClient();

    createResource(client, "users").post(u);
    client.destroy();
    params.add(new Object[] { new Credentials(u.getName(), u.getPassword()) });

    return params;
  }

  /**
   * Method description
   *
   */
  @BeforeClass
  public static void setup()
  {
    Group group = new Group("xml", "test-group");
    Client client = createAdminClient();

    createResource(client, "groups").post(group);
    client.destroy();
  }

  /**
   * Method description
   *
   */
  @Before
  public void login()
  {
    client = createClient();

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
  @After
  public void logout()
  {
    if (!credentials.isAnonymous())
    {
      logoutClient(client);
    }
  }

  /**
   * Method description
   *
   */
  @Test
  public void testConfig()
  {
    checkResponse(createResource(client, "config").get(ClientResponse.class));
    checkResponse(createResource(client, "config").post(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGitConfig()
  {
    checkResponse(
        createResource(client, "config/repositories/git").get(
          ClientResponse.class));
    checkResponse(
        createResource(client, "config/repositories/git").post(
          ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGroups()
  {
    checkResponse(createResource(client, "groups").get(ClientResponse.class));
    checkResponse(createResource(client, "groups").post(ClientResponse.class));
    checkResponse(
        createResource(client, "groups/test-group").get(ClientResponse.class));
    checkResponse(
        createResource(client, "groups/test-group").put(ClientResponse.class));
    checkResponse(
        createResource(client, "groups/test-group").delete(
          ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testHgConfig()
  {
    checkResponse(
        createResource(client, "config/repositories/hg").get(
          ClientResponse.class));
    checkResponse(
        createResource(client, "config/repositories/hg").post(
          ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testPlugins()
  {
    checkResponse(createResource(client, "plugins").get(ClientResponse.class));
    checkResponse(createResource(client,
                                 "plugins/overview").get(ClientResponse.class));
    checkResponse(
        createResource(client, "plugins/installed").get(ClientResponse.class));
    checkResponse(createResource(client,
                                 "plugins/updates").get(ClientResponse.class));
    checkResponse(
        createResource(client, "plugins/available").get(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSvnConfig()
  {
    checkResponse(
        createResource(client, "config/repositories/svn").get(
          ClientResponse.class));
    checkResponse(
        createResource(client, "config/repositories/svn").post(
          ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testUsers()
  {
    checkResponse(createResource(client, "users").get(ClientResponse.class));
    checkResponse(createResource(client, "users").post(ClientResponse.class));
    checkResponse(createResource(client,
                                 "users/scmadmin").get(ClientResponse.class));
    checkResponse(createResource(client,
                                 "users/scmadmin").put(ClientResponse.class));
    checkResponse(
        createResource(client, "users/scmadmin").delete(ClientResponse.class));
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  private void checkResponse(ClientResponse response)
  {
    assertNotNull(response);

    if (credentials.isAnonymous())
    {
      assertEquals(401, response.getStatus());
    }
    else
    {
      assertEquals(403, response.getStatus());
    }

    // fix jersey-client bug
    IOUtil.close(response.getEntityInputStream());
    response.close();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Client client;

  /** Field description */
  private Credentials credentials;
}
