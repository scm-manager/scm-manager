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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class RepositoryITCase extends AbstractAdminITCaseBase
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryType
   */
  public RepositoryITCase(String repositoryType)
  {
    this.repositoryType = repositoryType;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Parameters
  public static Collection<String[]> createParameters()
  {
    Collection<String[]> params = new ArrayList<String[]>();

    params.add(new String[] { "git" });
    params.add(new String[] { "svn" });

    if (IOUtil.search("hg") != null)
    {
      params.add(new String[] { "hg" });
    }

    return params;
  }

  /**
   * Method description
   *
   */
  @Test
  public void create()
  {
    Repository repository =
      RepositoryTestData.createHeartOfGold(repositoryType);

    createRepository(repository);
  }

  /**
   * Method description
   *
   */
  @Test
  public void delete()
  {
    Repository repository =
      RepositoryTestData.createHappyVerticalPeopleTransporter(repositoryType);

    repository = createRepository(repository);
    deleteRepository(repository.getId());
  }

  /**
   * Method description
   *
   */

  // @Test
  public void doubleCreate()
  {
    Repository repository = RepositoryTestData.create42Puzzle(repositoryType);

    repository = createRepository(repository);

    // repository = createRepository(repository);
  }

  /**
   * Method description
   *
   */
  @Test
  public void modify()
  {
    Repository repository =
      RepositoryTestData.createHappyVerticalPeopleTransporter(repositoryType);

    repository = createRepository(repository);
    repository.setPermissions(Arrays.asList(new Permission("dent",
            PermissionType.READ), new Permission("slarti",
              PermissionType.WRITE)));

    WebResource wr = createResource(client,
                                    "repositories/".concat(repository.getId()));
    ClientResponse response = wr.put(ClientResponse.class, repository);

    assertNotNull(response);
    assertTrue(response.getStatus() == 204);
    response.close();

    Repository other = getRepositoryById(repository.getId());

    assertRepositoriesEquals(repository, other);
    deleteRepository(repository.getId());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void getAll()
  {
    Repository repository =
      RepositoryTestData.createHappyVerticalPeopleTransporter(repositoryType);

    repository = createRepository(repository);

    WebResource wr = createResource(client, "repositories");
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);
    assertTrue(response.getStatus() == 200);

    Collection<Repository> repositories =
      response.getEntity(new GenericType<Collection<Repository>>() {}
    );

    response.close();
    assertNotNull(repositories);
    assertFalse(repositories.isEmpty());

    Repository hvpt = null;

    for (Repository other : repositories)
    {
      if (repository.equals(other))
      {
        hvpt = other;

        break;
      }
    }

    assertNotNull(hvpt);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param other
   */
  private void assertRepositoriesEquals(Repository repository, Repository other)
  {
    assertEquals(repository.getName(), other.getName());
    assertEquals(repository.getDescription(), other.getDescription());
    assertEquals(repository.getContact(), other.getContact());
    assertEquals(repository.getPermissions(), other.getPermissions());
    assertEquals(repository.getType(), other.getType());
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private Repository createRepository(Repository repository)
  {
    WebResource wr = createResource(client, "repositories");
    ClientResponse response = wr.post(ClientResponse.class, repository);

    assertNotNull(response);
    assertTrue(response.getStatus() == 201);

    String url = response.getHeaders().get("Location").get(0);

    response.close();

    Repository other = getRepository(url);

    assertNotNull(other);
    assertNotNull(other.getType());
    assertRepositoriesEquals(repository, other);
    assertNotNull(other.getId());
    assertNotNull(other.getCreationDate());

    return other;
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  private void deleteRepository(String id)
  {
    WebResource wr = createResource(client, "repositories/".concat(id));
    ClientResponse response = wr.delete(ClientResponse.class);

    assertNotNull(response);
    assertTrue(response.getStatus() == 204);
    response.close();
    wr = createResource(client, "repositories/".concat(id));
    response = wr.get(ClientResponse.class);
    assertNotNull(response);
    assertTrue(response.getStatus() == 404);
    response.close();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   *
   * @param url
   *
   * @return
   */
  private Repository getRepository(String url)
  {
    WebResource wr = client.resource(url);
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);

    Repository repository = response.getEntity(Repository.class);

    response.close();
    assertNotNull(repository);

    return repository;
  }

  /**
   * Method description
   *
   *
   *
   *
   *
   * @param id
   *
   * @return
   */
  private Repository getRepositoryById(String id)
  {
    WebResource wr = createResource(client, "repositories/".concat(id));
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);

    Repository repository = response.getEntity(Repository.class);

    response.close();
    assertNotNull(repository);

    return repository;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String repositoryType;
}
