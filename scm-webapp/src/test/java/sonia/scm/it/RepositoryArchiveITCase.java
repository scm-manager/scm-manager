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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.client.RepositoryClientException;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class RepositoryArchiveITCase extends RepositoryTypeITCaseBase
{

  /**
   * Constructs ...
   *
   *
   * @param type
   */
  public RepositoryArchiveITCase(String type)
  {
    this.type = type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void createTestRepository()
  {
    repository = RepositoryTestData.createHeartOfGold(type);
    client = createAdminClient();
    repository = createRepository(client, repository);
  }

  /**
   * Method description
   *
   */
  @After
  public void deleteTestRepository()
  {
    if (repository != null)
    {
      setArchiveMode(false);
      deleteRepository(client, repository.getId());
    }

    logoutClient(client);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDeleteAllowed()
  {
    setArchiveMode(true);

    WebResource resource = createResource(client,
                             "repositories/".concat(repository.getId()));

    repository.setArchived(true);

    ClientResponse response = resource.put(ClientResponse.class, repository);

    assertNotNull(response);
    assertEquals(204, response.getStatus());
    response = resource.delete(ClientResponse.class);
    assertNotNull(response);
    assertEquals(204, response.getStatus());
    repository = null;
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDeleteDenied()
  {
    setArchiveMode(true);

    WebResource resource = createResource(client,
                             "repositories/".concat(repository.getId()));
    ClientResponse response = resource.delete(ClientResponse.class);

    assertNotNull(response);
    assertEquals(412, response.getStatus());
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param archive
   */
  private void setArchiveMode(boolean archive)
  {
    WebResource resource = createResource(client, "config");
    ScmConfiguration config = resource.get(ScmConfiguration.class);

    assertNotNull(config);
    config.setEnableRepositoryArchive(archive);

    ClientResponse resp = resource.post(ClientResponse.class, config);

    assertNotNull(resp);
    assertEquals(201, resp.getStatus());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Client client;

  /** Field description */
  private Repository repository;

  /** Field description */
  private String type;
}
