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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.inject.util.Providers.of;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamespaceRootResourceTest {

  @Mock
  RepositoryManager repositoryManager;

  RestDispatcher dispatcher = new RestDispatcher();
  MockHttpResponse response = new MockHttpResponse();

  ResourceLinks links = ResourceLinksMock.createMock(URI.create("/"));

  @BeforeEach
  void setUpResources() {
    NamespaceToNamespaceDtoMapper namespaceMapper = new NamespaceToNamespaceDtoMapper(links);
    NamespaceCollectionToDtoMapper namespaceCollectionToDtoMapper = new NamespaceCollectionToDtoMapper(namespaceMapper, links);
    NamespaceCollectionResource namespaceCollectionResource = new NamespaceCollectionResource(repositoryManager, namespaceCollectionToDtoMapper);
    NamespaceResource namespaceResource = new NamespaceResource(repositoryManager, namespaceMapper);
    dispatcher.addSingletonResource(new NamespaceRootResource(of(namespaceCollectionResource), of(namespaceResource)));
  }

  @Test
  void shouldReturnAllNamespaces() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.getAllNamespaces()).thenReturn(asList("hitchhiker", "space"));

    MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString())
      .contains("\"self\":{\"href\":\"/v2/namespaces/\"}")
      .contains("\"_embedded\"")
      .contains("\"namespace\":\"hitchhiker\"")
      .contains("\"namespace\":\"space\"");
  }

  @Test
  void shouldReturnSingleNamespace() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.getAllNamespaces()).thenReturn(asList("hitchhiker", "space"));

    MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "space");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString())
      .contains("\"namespace\":\"space\"")
      .contains("\"self\":{\"href\":\"/v2/namespaces/space\"}");
  }

  @Test
  void shouldHandleUnknownNamespace() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.getAllNamespaces()).thenReturn(asList("hitchhiker", "space"));

    MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "unknown");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(404);
  }
}
