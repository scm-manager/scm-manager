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
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import sonia.scm.repository.Repository;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

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
@RunWith(Parameterized.class)
public class RepositorySimplePermissionITCase
        extends AbstractPermissionITCaseBase<Repository>
{

  /** Field description */
  private static String REPOSITORY_UUID;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param credentials
   */
  public RepositorySimplePermissionITCase(Credentials credentials)
  {
    super(credentials);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @BeforeClass
  public static void createTestRepository()
  {
    Repository repository = new Repository();

    repository.setName("test-repo");
    repository.setType("git");
    repository.setPublicReadable(false);

    Client client = createClient();

    authenticateAdmin(client);

    WebResource wr = createResource(client, "repositories");
    ClientResponse response = wr.post(ClientResponse.class, repository);

    assertNotNull(response);
    assertEquals(201, response.getStatus());

    String repositoryUrl = response.getHeaders().getFirst("Location");

    assertNotNull(repositoryUrl);
    response.close();
    wr = client.resource(repositoryUrl);
    response = wr.get(ClientResponse.class);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    repository = response.getEntity(Repository.class);
    assertNotNull(repository);
    REPOSITORY_UUID = repository.getId();
    assertNotNull(REPOSITORY_UUID);
    response.close();
    logoutClient(client);
    client.destroy();
  }

  /**
   * Method description
   *
   */
  @AfterClass
  public static void removeTestRepoistory()
  {
    Client client = createClient();

    authenticateAdmin(client);
    createResource(client, "repositories/" + REPOSITORY_UUID).delete();
    client.destroy();
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  @Override
  protected void checkGetAllResponse(ClientResponse response)
  {
    if (!credentials.isAnonymous())
    {
      assertNotNull(response);
      assertEquals(200, response.getStatus());

      Collection<Repository> repositories =
        response.getEntity(new GenericType<Collection<Repository>>() {}
      );

      assertNotNull(repositories);
      assertTrue(repositories.isEmpty());
      response.close();
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  @Override
  protected void checkGetResponse(ClientResponse response)
  {
    if (!credentials.isAnonymous())
    {
      assertNotNull(response);
      assertEquals(403, response.getStatus());
      response.close();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getBasePath()
  {
    return "repositories";
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Repository getCreateItem()
  {
    Repository repository = new Repository();

    repository.setName("create-test-repo");
    repository.setType("svn");

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getDeletePath()
  {
    return "repositories/".concat(REPOSITORY_UUID);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getGetPath()
  {
    return "repositories/".concat(REPOSITORY_UUID);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Repository getModifyItem()
  {
    Repository repository = new Repository();

    repository.setName("test-repo");
    repository.setType("git");
    repository.setDescription("Test Repository");

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getModifyPath()
  {
    return "repositories/".concat(REPOSITORY_UUID);
  }
}
