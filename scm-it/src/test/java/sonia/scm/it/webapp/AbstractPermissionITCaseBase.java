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

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.it.utils.TestData;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.util.Collection;

import static java.util.Arrays.asList;
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

  @BeforeClass
  public static void cleanup() {
    TestData.cleanup();
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

    Response response = UserITUtil.postUser(client, trillian);

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
    Invocation.Builder wr = createResource(client, getDeletePath());

    checkResponse(wr.delete(Response.class));
  }

  /**
   * Method description
   *
   */
  @Test
  public void modify()
  {
    Invocation.Builder wr = createResource(client, getModifyPath(), getMediaType());

    checkResponse(wr.put(Entity.entity(getModifyItem(), getMediaType()), Response.class));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void get()
  {
    Invocation.Builder wr = createResource(client, getGetPath());

    checkGetResponse(wr.buildGet().invoke());
  }

  /**
   * Method description
   *
   */
  @Test
  public void getAll()
  {
    Invocation.Builder wr = createResource(client, getBasePath());

    checkGetAllResponse(wr.buildGet().invoke());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   */
  protected void checkGetAllResponse(Response response)
  {
    checkResponse(response);
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  protected void checkGetResponse(Response response)
  {
    checkResponse(response);
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  private void checkResponse(Response response)
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
