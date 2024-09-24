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


import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.Assert;
import sonia.scm.api.v2.resources.ConfigDto;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.web.VndMediaType;

import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.webapp.ConfigUtil.readConfig;
import static sonia.scm.it.webapp.IntegrationTestUtil.BASE_URL;
import static sonia.scm.it.webapp.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.IntegrationTestUtil.getLink;

public final class RepositoryITUtil
{

  private RepositoryITUtil() {}

  public static void setNamespaceStrategy(ScmClient client, String namespaceStrategy) {
    ConfigDto config = readConfig(client);

    config.setNamespaceStrategy(namespaceStrategy);

    ConfigUtil.writeConfig(client, config);
  }

  public static RepositoryDto createRepository(ScmClient client, String repositoryJson) {
    return createRepository(client, repositoryJson, "UsernameNamespaceStrategy");
  }

  public static RepositoryDto createRepository(ScmClient client, String repositoryJson, String namespaceStrategy) {
    setNamespaceStrategy(createAdminClient(), namespaceStrategy);

    Response response =
      createResource(client, "repositories")
        .accept("*/*")
        .post(Entity.entity(repositoryJson, MediaType.valueOf(VndMediaType.REPOSITORY)));

    assertNotNull(response);
    Assert.assertEquals(201, response.getStatus());

    URI url = URI.create(response.getHeaders().get("Location").get(0).toString());

    response.close();

    RepositoryDto other = getRepository(client, url);

    assertNotNull(other);
    assertNotNull(other.getType());
    assertNotNull(other.getNamespace());
    assertNotNull(other.getCreationDate());

    return other;
  }

  public static void deleteRepository(ScmClient client, RepositoryDto repository)
  {
    URI deleteUrl = getLink(repository, "delete");
    Response response = createResource(client, deleteUrl).delete(Response.class);

    assertNotNull(response);
    Assert.assertEquals(204, response.getStatus());
    response.close();

    URI selfUrl = getLink(repository, "self");
    response = createResource(client, selfUrl).get(Response.class);
    assertNotNull(response);
    Assert.assertEquals(404, response.getStatus());
    response.close();
  }

  public static RepositoryDto getRepository(ScmClient client, URI url)
  {
    Invocation.Builder wr = createResource(client, url);
    Response response = wr.buildGet().invoke();

    assertNotNull(response);
    Assert.assertEquals(200, response.getStatus());

    RepositoryDto repository = response.readEntity(RepositoryDto.class);

    response.close();
    assertNotNull(repository);

    return repository;
  }

  public static String createUrl(RepositoryDto repository) {
    return BASE_URL + repository.getType() + "/" + repository.getNamespace() + "/" + repository.getName();
  }
}
