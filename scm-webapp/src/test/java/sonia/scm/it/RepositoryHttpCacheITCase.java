/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryHttpCacheITCase extends HttpCacheITCaseBase<Repository>
{

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Repository createSampleItem()
  {
    Repository repository = RepositoryTestData.createHeartOfGold("git");
    Client client = createAdminClient();
    WebResource resource = createResource(client, "repositories");
    ClientResponse response = resource.post(ClientResponse.class, repository);

    assertNotNull(response);
    assertEquals(201, response.getStatus());

    String location = response.getHeaders().get("Location").get(0);

    assertNotNull(location);
    resource = client.resource(location);
    response = resource.get(ClientResponse.class);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    repository = response.getEntity(Repository.class);
    assertNotNull(repository);
    assertNotNull(repository.getId());

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  protected void destroy(Repository item)
  {
    Client client = createAdminClient();
    WebResource resource = createResource(client,
                             "repositories/".concat(item.getId()));
    ClientResponse response = resource.delete(ClientResponse.class);

    assertNotNull(response);
    assertEquals(204, response.getStatus());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getCollectionUrlPart()
  {
    return "repositories";
  }
}
