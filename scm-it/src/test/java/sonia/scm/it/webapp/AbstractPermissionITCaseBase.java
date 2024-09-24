/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.it.webapp;


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


public abstract class AbstractPermissionITCaseBase<T>
{


  public AbstractPermissionITCaseBase(Credentials credentials)
  {
    this.credentials = credentials;
    this.client = credentials.isAnonymous()? ScmClient.anonymous(): new ScmClient(credentials.getUsername(), credentials.getPassword());
  }

  @BeforeClass
  public static void cleanup() {
    TestData.cleanup();
  }


  
  @Parameters(name = "{1}")
  public static Collection<Object[]> createParameters()
  {
    return asList(
      new Object[] {new Credentials(), "anonymous"},
      new Object[] {new Credentials("trillian", "a.trillian124"), "trillian" }
    );
  }

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

  @AfterClass
  public static void removeTestUser()
  {
    ScmClient client = createAdminClient();
    createResource(client, "users/trillian").delete();
  }
  
  protected abstract String getBasePath();

  protected abstract T getCreateItem();

  protected abstract String getDeletePath();
  
  protected abstract String getGetPath();
  
  protected abstract T getModifyItem();
  
  protected abstract String getModifyPath();

  protected abstract String getMediaType();


   @Test
  public void create()
  {
    checkResponse(post(client, getBasePath(), getMediaType(), getCreateItem()));
  }

   @Test
  public void delete()
  {
    Invocation.Builder wr = createResource(client, getDeletePath());

    checkResponse(wr.delete(Response.class));
  }

   @Test
  public void modify()
  {
    Invocation.Builder wr = createResource(client, getModifyPath(), getMediaType());

    checkResponse(wr.put(Entity.entity(getModifyItem(), getMediaType()), Response.class));
  }


   @Test
  public void get()
  {
    Invocation.Builder wr = createResource(client, getGetPath());

    checkGetResponse(wr.buildGet().invoke());
  }

   @Test
  public void getAll()
  {
    Invocation.Builder wr = createResource(client, getBasePath());

    checkGetAllResponse(wr.buildGet().invoke());
  }



  protected void checkGetAllResponse(Response response)
  {
    checkResponse(response);
  }


  protected void checkGetResponse(Response response)
  {
    checkResponse(response);
  }


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
