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


import de.otto.edison.hal.HalRepresentation;
import jakarta.ws.rs.core.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static sonia.scm.it.webapp.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.IntegrationTestUtil.post;


@RunWith(Parameterized.class)
public class RepositorySimplePermissionITCase
        extends AbstractPermissionITCaseBase<RepositoryDto>
{

  private static String REPOSITORY_PATH;


 
  public RepositorySimplePermissionITCase(Credentials credentials, String ignore_testCaseName)
  {
    super(credentials);
  }


   @BeforeClass
  public static void createTestRepository() throws IOException {
    RepositoryDto repository = new RepositoryDto();

    repository.setName("test-repo");
    repository.setType("git");

    ScmClient client = createAdminClient();

    String repositoryUrl;
    try (Response response = post(client, "repositories", VndMediaType.REPOSITORY, repository)) {

      assertNotNull(response);
      Assert.assertEquals(201, response.getStatus());

      repositoryUrl = response.getHeaders().getFirst("Location").toString();

      assertNotNull(repositoryUrl);
    }

    Response clientResponse = client.resource(repositoryUrl).get();
    assertNotNull(clientResponse);
    Assert.assertEquals(200, clientResponse.getStatus());
    repository = clientResponse.readEntity(RepositoryDto.class);
    REPOSITORY_PATH = repository.getNamespace() + "/" + repository.getName();
    assertNotNull(REPOSITORY_PATH);
    clientResponse.close();
  }

   @AfterClass
  public static void removeTestRepository()
  {
    createResource(createAdminClient(), "repositories/" + REPOSITORY_PATH).delete();
  }


  @Override
  protected void checkGetAllResponse(Response response)
  {
    if (!credentials.isAnonymous())
    {
      assertNotNull(response);
      Assert.assertEquals(200, response.getStatus());

      HalRepresentation repositories = response.readEntity(HalRepresentation.class);

      assertNotNull(repositories);
      assertTrue(repositories.getEmbedded().getItemsBy("repositories").isEmpty());
      response.close();
    }
  }


  @Override
  protected void checkGetResponse(Response response)
  {
    if (!credentials.isAnonymous())
    {
      assertNotNull(response);
      Assert.assertEquals(403, response.getStatus());
      response.close();
    }
  }


  
  @Override
  protected String getBasePath()
  {
    return "repositories";
  }

  
  @Override
  protected RepositoryDto getCreateItem()
  {
    RepositoryDto repository = new RepositoryDto();

    repository.setName("create-test-repo");
    repository.setType("svn");

    return repository;
  }

  
  @Override
  protected String getDeletePath()
  {
    return "repositories/".concat(REPOSITORY_PATH);
  }

  
  @Override
  protected String getGetPath()
  {
    return "repositories/".concat(REPOSITORY_PATH);
  }

  
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
