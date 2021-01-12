/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.it.webapp;

//~--- non-JDK imports --------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.webapp.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.IntegrationTestUtil.post;

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
    Assert.assertEquals(201, response.getStatus());
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
      Assert.assertEquals(401, response.getStatus());
    }
    else
    {
      Assert.assertEquals(403, response.getStatus());
    }

    response.close();
  }

  //~--- fields ---------------------------------------------------------------

  protected ScmClient client;

  protected Credentials credentials;
}
