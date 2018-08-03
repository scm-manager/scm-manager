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
import de.otto.edison.hal.HalRepresentation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static sonia.scm.it.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.IntegrationTestUtil.createResource;
import static sonia.scm.it.IntegrationTestUtil.serialize;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class RepositorySimplePermissionITCase
        extends AbstractPermissionITCaseBase<RepositoryDto>
{

  /** Field description */
  private static String REPOSITORY_PATH;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param credentials
   */
  public RepositorySimplePermissionITCase(Credentials credentials, String ignore_testCaseName)
  {
    super(credentials);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @BeforeClass
  public static void createTestRepository() throws IOException {
    RepositoryDto repository = new RepositoryDto();

    repository.setName("test-repo");
    repository.setType("git");
//    repository.setPublicReadable(false);

    ScmClient client = createAdminClient();

    WebResource.Builder wr = createResource(client, "repositories");
    ClientResponse response = wr.type(VndMediaType.REPOSITORY).post(ClientResponse.class, serialize(repository));

    assertNotNull(response);
    assertEquals(201, response.getStatus());

    String repositoryUrl = response.getHeaders().getFirst("Location");

    assertNotNull(repositoryUrl);
    response.close();
    response = client.resource(repositoryUrl).get(ClientResponse.class);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    repository = new ObjectMapperProvider().get().readValue(response.getEntity(String.class), RepositoryDto.class);
    REPOSITORY_PATH = repository.getNamespace() + "/" + repository.getName();
    assertNotNull(REPOSITORY_PATH);
    response.close();
  }

  /**
   * Method description
   *
   */
  @AfterClass
  public static void removeTestRepository()
  {
    createResource(createAdminClient(), "repositories/" + REPOSITORY_PATH).delete();
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

      HalRepresentation repositories =
        null;
      try {
        repositories = new ObjectMapperProvider().get().readValue(response.getEntity(String.class), HalRepresentation.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      assertNotNull(repositories);
      assertTrue(repositories.getEmbedded().getItemsBy("repositories").isEmpty());
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
  protected RepositoryDto getCreateItem()
  {
    RepositoryDto repository = new RepositoryDto();

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
    return "repositories/".concat(REPOSITORY_PATH);
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
    return "repositories/".concat(REPOSITORY_PATH);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected RepositoryDto getModifyItem()
  {
    RepositoryDto repository = new RepositoryDto();

    repository.setName("test-repo");
    repository.setNamespace("scmadmin");
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
    return "repositories/".concat(REPOSITORY_PATH);
  }

  @Override
  protected String getMediaType() {
    return VndMediaType.REPOSITORY;
  }
}
