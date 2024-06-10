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

import com.google.inject.util.Providers;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BranchDetails;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BranchDetailsCommandBuilder;
import sonia.scm.repository.api.BranchDetailsCommandResult;
import sonia.scm.repository.api.CommandNotSupportedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchDetailsResourceTest extends RepositoryTestBase {

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

  private final JsonMockHttpResponse response = new JsonMockHttpResponse();

  @BeforeEach
  void prepareEnvironment() {
    super.branchDetailsResource = new BranchDetailsResource(serviceFactory, mapper, resourceLinks);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/scm/api/"));
    mapper.setResourceLinks(new ResourceLinks(Providers.of(scmPathInfoStore)));
  }

  @Test
  void shouldReturnBadRequestIfBranchDetailsNotSupported() throws URISyntaxException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    doThrow(CommandNotSupportedException.class).when(service).getBranchDetailsCommand();

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details/master/");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldReturnBranchDetails() throws URISyntaxException, UnsupportedEncodingException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    when(service.getRepository()).thenReturn(repository);
    when(service.getBranchDetailsCommand()).thenReturn(branchDetailsCommandBuilder);
    BranchDetailsCommandResult result = new BranchDetailsCommandResult(new BranchDetails("master", 42, 21));
    when(branchDetailsCommandBuilder.execute("master")).thenReturn(result);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details/master/");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString())
      .isEqualTo("{\"branchName\":\"master\",\"changesetsAhead\":42,\"changesetsBehind\":21,\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/master\"}}}");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "%2Fmaster",
  })
  void shouldValidateSingleBranch(String branchName) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + String.format("/branch-details/%s/", branchName));

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldGetEmptyDetailsCollection() throws URISyntaxException, UnsupportedEncodingException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details/");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).isEqualTo("{\"_links\":{\"self\":{\"href\":\"/v2/repositories/hitchhiker/42Puzzle/branch-details/\"}},\"_embedded\":{\"branchDetails\":[]}}");
  }

  @Test
  void shouldGetDetailsCollection() throws URISyntaxException, UnsupportedEncodingException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    when(service.getRepository()).thenReturn(repository);
    when(service.getBranchDetailsCommand()).thenReturn(branchDetailsCommandBuilder);
    when(branchDetailsCommandBuilder.execute(any())).thenAnswer(invocation -> new BranchDetailsCommandResult(new BranchDetails(invocation.getArgument(0, String.class), null, null)));

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details?branches=master&branches=develop&branches=feature%2Fhitchhiker42");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).contains("{\"branchDetails\":[{\"branchName\":\"master\",\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/master\"}}}");
    assertThat(response.getContentAsString()).contains("{\"branchName\":\"develop\",\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/develop\"}}}");
    assertThat(response.getContentAsString()).contains("{\"branchName\":\"feature/hitchhiker42\",\"_links\":{\"self\":{\"href\":\"/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/feature%2Fhitchhiker42\"}}}");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "",
    "%2Fmaster",
  })
  void shouldRejectInvalidBranchInCollection(String branchName) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + String.format("/branch-details?branches=ok&branches=%s", branchName));

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldIgnoreMissingBranchesInCollection() throws URISyntaxException {
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    when(service.getBranchDetailsCommand()).thenReturn(branchDetailsCommandBuilder);
    when(branchDetailsCommandBuilder.execute("no-such-branch")).thenThrow(NotFoundException.class);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + repository.getNamespaceAndName() + "/branch-details?branches=no-such-branch");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsJson().get("_embedded").get("branchDetails")).isEmpty();
  }
}

