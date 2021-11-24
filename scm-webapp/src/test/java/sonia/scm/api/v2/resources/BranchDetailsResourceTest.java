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

package sonia.scm.api.v2.resources;

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BranchDetailsCommandBuilder;
import sonia.scm.repository.api.BranchDetailsCommandResult;
import sonia.scm.repository.api.CommandNotSupportedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SuppressWarnings("UnstableApiUsage")
@RunWith(MockitoJUnitRunner.class)
public class BranchDetailsResourceTest extends RepositoryTestBase {

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final Repository repository = RepositoryTestData.create42Puzzle();
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private BranchDetailsCommandBuilder branchDetailsCommandBuilder;
  private final BranchDetailsMapperImpl mapper = new BranchDetailsMapperImpl();

  private final MockHttpResponse response = new MockHttpResponse();

  @Before
  public void prepareEnvironment() {
    super.branchDetailsResource = new BranchDetailsResource(serviceFactory, mapper, resourceLinks);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/scm/api/"));
    mapper.setResourceLinks(new ResourceLinks(scmPathInfoStore));
  }

  @Test
  public void shouldReturnBadRequestIfBranchDetailsNotSupported() throws URISyntaxException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    doThrow(CommandNotSupportedException.class).when(service).getBranchDetailsCommand();

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details/master/");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void shouldReturnBranchDetails() throws URISyntaxException, UnsupportedEncodingException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    when(service.getRepository()).thenReturn(repository);
    when(service.getBranchDetailsCommand()).thenReturn(branchDetailsCommandBuilder);
    BranchDetailsCommandResult result = new BranchDetailsCommandResult(42, 21);
    when(branchDetailsCommandBuilder.execute("master")).thenReturn(result);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details/master/");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString())
      .isEqualTo("{\"branchName\":\"master\",\"changesetsAhead\":42,\"changesetsBehind\":21,\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/master\"}}}");
  }

  @Test
  public void shouldGetEmptyDetailsCollection() throws URISyntaxException, UnsupportedEncodingException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    when(service.getRepository()).thenReturn(repository);
    when(service.getBranchDetailsCommand()).thenReturn(branchDetailsCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details/");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).isEqualTo("{\"_links\":{\"self\":{\"href\":\"/v2/repositories/hitchhiker/42Puzzle/branch-details/\"}},\"_embedded\":{\"branchDetails\":[]}}");
  }

  @Test
  public void shouldGetDetailsCollection() throws URISyntaxException, UnsupportedEncodingException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    when(service.getRepository()).thenReturn(repository);
    when(service.getBranchDetailsCommand()).thenReturn(branchDetailsCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details?branches=master,develop,feature%2Fhitchhiker42");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).contains("{\"branchDetails\":[{\"branchName\":\"master\",\"changesetsAhead\":0,\"changesetsBehind\":0,\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/master\"}}}");
    assertThat(response.getContentAsString()).contains("{\"branchName\":\"develop\",\"changesetsAhead\":0,\"changesetsBehind\":0,\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/develop\"}}}");
    assertThat(response.getContentAsString()).contains("{\"branchName\":\"feature/hitchhiker42\",\"changesetsAhead\":0,\"changesetsBehind\":0,\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/feature%2Fhitchhiker42\"}}}");
  }
}

