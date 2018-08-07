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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.IntegrationTestUtil.createResource;
import static sonia.scm.it.IntegrationTestUtil.post;

//~--- JDK imports ------------------------------------------------------------

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
    this.client = credentials.isAnonymous()? ScmClient.anonymous(): new ScmClient(credentials.getUsername(), credentials.getPassword());
  }


  //~--- methods --------------------------------------------------------------
  /**
   * Method description
   *
   *
   * @return
   */
  @Parameters(name = "{1}")
  public static Collection<Object[]> createParameters()
  {
    return asList(
      new Object[] {new Credentials(), "anonymous"},
      new Object[] {new Credentials("trillian", "a.trillian124"), "trillian" }
    );
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

    ScmClient client = createAdminClient();

    ClientResponse response = UserITUtil.postUser(client, trillian);

    assertNotNull(response);
    assertEquals(201, response.getStatus());
    response.close();
  }

  /**
   * Method description
   *
   */
  @AfterClass
  public static void removeTestUser()
  {
    ScmClient client = createAdminClient();
    createResource(client, "users/trillian").delete();
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

  protected abstract String getMediaType();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void create()
  {
    checkResponse(post(client, getBasePath(), getMediaType(), getCreateItem()));
  }

  /**
   * Method description
   *
   */
  @Test
  public void delete()
  {
    WebResource.Builder wr = createResource(client, getDeletePath());

    checkResponse(wr.delete(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void modify()
  {
    WebResource.Builder wr = createResource(client, getModifyPath());

    checkResponse(wr.type(getMediaType()).put(ClientResponse.class, getModifyItem()));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void get()
  {
    WebResource.Builder wr = createResource(client, getGetPath());

    checkGetResponse(wr.get(ClientResponse.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void getAll()
  {
    WebResource.Builder wr = createResource(client, getBasePath());

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

  //~--- fields ---------------------------------------------------------------

  protected ScmClient client;

  protected Credentials credentials;
}
