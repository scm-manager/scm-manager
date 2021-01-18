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
import org.junit.Assert;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static sonia.scm.it.webapp.IntegrationTestUtil.BASE_URL;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.IntegrationTestUtil.getLink;

//~--- JDK imports ------------------------------------------------------------

public final class RepositoryITUtil
{

  private RepositoryITUtil() {}

  public static RepositoryDto createRepository(ScmClient client, String repositoryJson) {
    ClientResponse response =
      createResource(client, "repositories")
        .accept("*/*")
        .type(VndMediaType.REPOSITORY)
        .post(ClientResponse.class, repositoryJson);

    assertNotNull(response);
    Assert.assertEquals(201, response.getStatus());

    URI url = URI.create(response.getHeaders().get("Location").get(0));

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
    ClientResponse response = createResource(client, deleteUrl).delete(ClientResponse.class);

    assertNotNull(response);
    Assert.assertEquals(204, response.getStatus());
    response.close();

    URI selfUrl = getLink(repository, "self");
    response = createResource(client, selfUrl).get(ClientResponse.class);
    assertNotNull(response);
    Assert.assertEquals(404, response.getStatus());
    response.close();
  }

  public static RepositoryDto getRepository(ScmClient client, URI url)
  {
    WebResource.Builder wr = createResource(client, url);
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);
    Assert.assertEquals(200, response.getStatus());

    String json = response.getEntity(String.class);
    RepositoryDto repository = null;
    try {
      repository = new ObjectMapperProvider().get().readerFor(RepositoryDto.class).readValue(json);
    } catch (IOException e) {
      fail("could not read json:\n" + json);
    }

    response.close();
    assertNotNull(repository);

    return repository;
  }

  public static String createUrl(RepositoryDto repository) {
    return BASE_URL + repository.getType() + "/" + repository.getNamespace() + "/" + repository.getName();
  }
}
