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
public class CreateRepositoriesITCase extends AbstractAdminITCaseBase
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryType
   */
  public CreateRepositoriesITCase(String repositoryType)
  {
    System.out.append("==> CreateRepositoriesITCase - ").println(
        repositoryType);
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
    params.add(new String[] { "git" });

    if (IOUtil.search("hg") != null)
    {
      params.add(new String[] { "git" });
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
  public void testCreate()
  {
    repository = RepositoryTestData.createHeartOfGold(repositoryType);
    repository = createRepository(client, repository);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateAllreadyExists()
  {
    repository = RepositoryTestData.create42Puzzle(repositoryType);
    repository = createRepository(client, repository);

    WebResource wr = createResource(client, "repositories");
    ClientResponse response =
      wr.post(ClientResponse.class,
              RepositoryTestData.create42Puzzle(repositoryType));

    assertNotNull(response);
    assertEquals(500, response.getStatus());
    response.close();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateAllreadyExistsWithStructure()
  {
    repository = RepositoryTestData.create42Puzzle(repositoryType);
    repository = createRepository(client, repository);

    Repository r = RepositoryTestData.create42Puzzle(repositoryType);

    r.setName(r.getName() + "/" + r.getName());

    WebResource wr = createResource(client, "repositories");
    ClientResponse response = wr.post(ClientResponse.class, r);

    assertNotNull(response);
    System.out.println( response.getStatus() );
    assertEquals(500, response.getStatus());
    response.close();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateWithStructure()
  {
    repository = RepositoryTestData.createHeartOfGold(repositoryType);
    repository.setName("test/".concat(repository.getName()));
    repository = createRepository(client, repository);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Repository repository;

  /** Field description */
  private String repositoryType;
}
