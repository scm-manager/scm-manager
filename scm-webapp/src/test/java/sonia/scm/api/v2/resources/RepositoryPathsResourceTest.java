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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryPathCollector;
import sonia.scm.repository.RepositoryPaths;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPathsResourceTest extends RepositoryTestBase {

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final NamespaceAndName PUZZLE_42 = new NamespaceAndName("hitchhiker", "puzzle-42");

  private final ObjectMapper mapper = new ObjectMapper();

  @Mock
  private RepositoryPathCollector collector;

  @InjectMocks
  private RepositoryPathsResource resource;

  @BeforeEach
  public void prepareEnvironment() {
    super.repositoryPathsResource = resource;
    dispatcher.addSingletonResource(getRepositoryRootResource());
  }

  @Test
  void shouldReturnCollectedPaths() throws IOException, URISyntaxException {
    mockCollector("21", "a.txt", "b/c.txt");

    MockHttpResponse response = request("21");

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

    RepositoryPathsDto dto = mapper.readValue(response.getContentAsString(), RepositoryPathsDto.class);
    assertThat(dto.getRevision()).isEqualTo("21");
    assertThat(dto.getPaths()).containsExactly("a.txt", "b/c.txt");
  }

  @Test
  void shouldAppendSelfLink() throws IOException, URISyntaxException {
    mockCollector("42");

    MockHttpResponse response = request("42");

    RepositoryPathsDto dto = mapper.readValue(response.getContentAsString(), RepositoryPathsDto.class);
    assertThat(dto.getLinks().getLinkBy("self")).isPresent().hasValueSatisfying(link ->
      assertThat(link.getHref()).isEqualTo("/v2/repositories/hitchhiker/puzzle-42/paths/42")
    );
  }

  private MockHttpResponse request(String revision) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "hitchhiker/puzzle-42/paths/" + revision);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private void mockCollector(String revision, String... paths) throws IOException {
    RepositoryPaths result = new RepositoryPaths(revision, Arrays.asList(paths));
    when(collector.collect(PUZZLE_42, revision)).thenReturn(result);
  }

}
