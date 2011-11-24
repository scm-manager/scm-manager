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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class GetRepositoriesITCase extends AbstractAdminITCaseBase
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryType
   */
  public GetRepositoriesITCase(String repositoryType)
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
   *
   * @throws IOException
   */
  @After
  public void cleanup() throws IOException
  {
    deleteRepository(client, repository.getId());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetById()
  {
    repository = RepositoryTestData.createHeartOfGold(repositoryType);
    repository = createRepository(client, repository);

    String id = repository.getId();

    assertNotNull(id);

    Repository r = getRepositoryById(client, id);

    assertEquals(id, r.getId());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetByTypeAndName()
  {
    repository = RepositoryTestData.create42Puzzle(repositoryType);
    testGetByTypeAndName(repository);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetByTypeAndNameWithDirectoryStructure()
  {
    repository =
      RepositoryTestData.createRestaurantAtTheEndOfTheUniverse(repositoryType);
    repository.setName("test/".concat(repository.getName()));
    testGetByTypeAndName(repository);
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  private void testGetByTypeAndName(Repository repo)
  {
    repository = createRepository(client, repo);

    String name = repository.getName();
    WebResource wr = createResource(
                         client,
                         "repositories/".concat(repositoryType).concat(
                           "/").concat(name));
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);

    Repository r = response.getEntity(Repository.class);

    response.close();
    assertNotNull(r);
    assertEquals(repository.getId(), r.getId());
    assertEquals(repository.getName(), r.getName());
    assertEquals(repository.getType(), r.getType());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Repository repository;

  /** Field description */
  private String repositoryType;
}
