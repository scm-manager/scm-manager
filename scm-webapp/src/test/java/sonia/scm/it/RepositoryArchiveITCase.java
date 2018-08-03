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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.api.v2.resources.ConfigDto;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.MediaType;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.IntegrationTestUtil.createResource;
import static sonia.scm.it.IntegrationTestUtil.getLink;
import static sonia.scm.it.IntegrationTestUtil.readJson;
import static sonia.scm.it.IntegrationTestUtil.serialize;
import static sonia.scm.it.RepositoryITUtil.createRepository;
import static sonia.scm.it.RepositoryITUtil.deleteRepository;

//~--- JDK imports ------------------------------------------------------------


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
  public void createTestRepository() {
    client = createAdminClient();
    repository = createRepository(client, readJson("repository-" + type + ".json"));
  }

  /**
   * Method description
   *
   */
  @After
  public void deleteTestRepository()
  {
    setArchiveMode(false);

    if (repository != null)
    {
      deleteRepository(client, repository);
    }
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDeleteAllowed() {
    setArchiveMode(true);

    repository.setArchived(true);

    ClientResponse response = createResource(client,
      "repositories/" + repository.getNamespace() + "/" + repository.getName())
      .type(VndMediaType.REPOSITORY).put(ClientResponse.class, serialize(repository));

    assertNotNull(response);
    assertEquals(204, response.getStatus());
    response = createResource(client,
      "repositories/" + repository.getNamespace() + "/" + repository.getName()).delete(ClientResponse.class);
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

    URI deleteUrl = getLink(repository, "delete");
    ClientResponse response = createResource(client, deleteUrl).delete(ClientResponse.class);

    assertNotNull(response);
    assertEquals(412, response.getStatus());
    response.close();
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
    WebResource.Builder resource = createResource(client, "config").type(MediaType.APPLICATION_JSON);
    ConfigDto config = resource.get(ConfigDto.class);

    assertNotNull(config);
    config.setEnableRepositoryArchive(archive);

    ClientResponse resp = createResource(client, "config").type(VndMediaType.CONFIG).put(ClientResponse.class, config);

    assertNotNull(resp);
    assertEquals(204, resp.getStatus());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmClient client;

  /** Field description */
  private RepositoryDto repository;

  /** Field description */
  private String type;
}
