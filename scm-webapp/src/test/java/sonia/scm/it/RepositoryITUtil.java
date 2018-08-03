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
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static sonia.scm.it.IntegrationTestUtil.BASE_URL;
import static sonia.scm.it.IntegrationTestUtil.createResource;
import static sonia.scm.it.IntegrationTestUtil.getLink;

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
    assertEquals(201, response.getStatus());

    URI url = URI.create(response.getHeaders().get("Location").get(0));

    response.close();

    RepositoryDto other = getRepository(client, url);

    assertNotNull(other);
    assertNotNull(other.getType());
    assertNotNull(other.getCreationDate());

    return other;
  }

  public static void deleteRepository(ScmClient client, RepositoryDto repository)
  {
    URI deleteUrl = getLink(repository, "delete");
    ClientResponse response = createResource(client, deleteUrl).delete(ClientResponse.class);

    assertNotNull(response);
    assertEquals(204, response.getStatus());
    response.close();

    URI selfUrl = getLink(repository, "self");
    response = createResource(client, selfUrl).get(ClientResponse.class);
    assertNotNull(response);
    assertEquals(404, response.getStatus());
    response.close();
  }

  public static RepositoryDto getRepository(ScmClient client, URI url)
  {
    WebResource.Builder wr = createResource(client, url);
    ClientResponse response = wr.get(ClientResponse.class);

    assertNotNull(response);
    assertEquals(200, response.getStatus());

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
