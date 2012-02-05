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
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

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
 *
 * @param <T>
 */
public abstract class AbstractPermissionITCaseBase<T>
{

  /**
   * Constructs ...
   *
   *
   *
   * @param credentials
   */
  public AbstractPermissionITCaseBase(Credentials credentials)
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
    params.add(new Credentials[] {
      new Credentials("trillian", "a.trillian124") });

    return params;
  }

  /**
   * Method description
   *
   *
   */
  @BeforeClass
  public static void createTestUser()
  {
    User trillian = UserTestData.createTrillian();

    trillian.setPassword("a.trillian124");

    Client client = createClient();

    authenticateAdmin(client);

    WebResource wr = createResource(client, "users");
    ClientResponse response = wr.post(ClientResponse.class, trillian);

    assertNotNull(response);
    assertEquals(201, response.getStatus());
    response.close();
    logoutClient(client);
    client.destroy();
  }

  /**
   * Method description
   *
   */
  @AfterClass
  public static void removeTestUser()
  {
    Client client = createClient();

    authenticateAdmin(client);
    createResource(client, "users/trillian").delete();
    client.destroy();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getBasePath();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract T getCreateItem();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getDeletePath();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getGetPath();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract T getModifyItem();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getModifyPath();

  //~--- methods --------------------------------------------------------------

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
    WebResource wr = createResource(client, getBasePath());

    checkResponse(wr.post(ClientResponse.class, getCreateItem()));
  }

  /**
   * Method description
   *
   */
  @Test
  public void delete()
  {
    WebResource wr = createResource(client, getDeletePath());

    checkResponse(wr.delete(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void modify()
  {
    WebResource wr = createResource(client, getModifyPath());

    checkResponse(wr.put(ClientResponse.class, getModifyItem()));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void get()
  {
    WebResource wr = createResource(client, getGetPath());

    checkGetResponse(wr.get(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void getAll()
  {
    WebResource wr = createResource(client, getBasePath());

    checkGetAllResponse(wr.get(ClientResponse.class));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   */
  protected void checkGetAllResponse(ClientResponse response)
  {
    checkResponse(response);
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  protected void checkGetResponse(ClientResponse response)
  {
    checkResponse(response);
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Client client;

  /** Field description */
  protected Credentials credentials;
}
