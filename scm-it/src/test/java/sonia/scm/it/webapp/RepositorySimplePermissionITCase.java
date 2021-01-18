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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.otto.edison.hal.HalRepresentation;
import org.junit.AfterClass;
import org.junit.Assert;
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
import static sonia.scm.it.webapp.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.IntegrationTestUtil.serialize;

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

    ScmClient client = createAdminClient();

    WebResource.Builder wr = createResource(client, "repositories");
    ClientResponse response = wr.type(VndMediaType.REPOSITORY).post(ClientResponse.class, serialize(repository));

    assertNotNull(response);
    Assert.assertEquals(201, response.getStatus());

    String repositoryUrl = response.getHeaders().getFirst("Location");

    assertNotNull(repositoryUrl);
    response.close();
    response = client.resource(repositoryUrl).get(ClientResponse.class);
    assertNotNull(response);
    Assert.assertEquals(200, response.getStatus());
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
      Assert.assertEquals(200, response.getStatus());

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
      Assert.assertEquals(403, response.getStatus());
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
