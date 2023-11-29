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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.util.Providers;
import de.otto.edison.hal.Links;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgRepositoryConfig;
import sonia.scm.repository.HgRepositoryConfigStore;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgRepositoryConfigResourceTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private RestDispatcher dispatcher;

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private HgRepositoryConfigStore store;

  @Mock
  private Subject subject;

  @BeforeEach
  void setUp() {
    ThreadContext.bind(subject);

    HgRepositoryConfigMapper mapper = createConfigMapper();
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(createRootResource(
        new HgRepositoryConfigResource(repositoryManager, store, mapper)
    ));
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  private HgConfigResource createRootResource(HgRepositoryConfigResource resource) {
    return new HgConfigResource(
      mock(HgGlobalConfigDtoToHgConfigMapper.class),
      mock(HgGlobalConfigToHgGlobalConfigDtoMapper.class),
      mock(HgRepositoryHandler.class),
      Providers.of(mock(HgGlobalConfigAutoConfigurationResource.class)),
      Providers.of(resource)
    );
  }

  private HgRepositoryConfigMapper createConfigMapper() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    HgConfigLinks links = new HgConfigLinks(pathInfoStore);
    HgRepositoryConfigMapper mapper = Mappers.getMapper(HgRepositoryConfigMapper.class);
    mapper.setLinks(links);
    return mapper;
  }

  @Test
  void shouldGetConfig() throws IOException, URISyntaxException {
    HgRepositoryConfig config = new HgRepositoryConfig();
    config.setEncoding("ISO-8859-15");

    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    when(repositoryManager.get(new NamespaceAndName("hitchhiker", "hog"))).thenReturn(repository);
    when(store.of(repository)).thenReturn(config);

    MockHttpRequest request = MockHttpRequest.get("/v2/config/hg/hitchhiker/hog");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

    JsonNode node = objectMapper.readTree(response.getContentAsString());
    assertThat(node.get("encoding").asText()).isEqualTo("ISO-8859-15");
    assertThat(node.get("_links").get("self").get("href").asText()).isEqualTo("/v2/config/hg/hitchhiker/HeartOfGold");
  }

  @Test
  void shouldUpdateConfig() throws IOException, URISyntaxException {
    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    when(repositoryManager.get(new NamespaceAndName("hitchhiker", "hog"))).thenReturn(repository);

    HgRepositoryConfigDto dto = new HgRepositoryConfigDto(Links.emptyLinks());
    dto.setEncoding("UTF-8");
    MockHttpRequest request = MockHttpRequest.put("/v2/config/hg/hitchhiker/hog").contentType(
      HgVndMediaType.REPO_CONFIG
    ).content(objectMapper.writeValueAsBytes(dto));
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    ArgumentCaptor<HgRepositoryConfig> captor = ArgumentCaptor.forClass(HgRepositoryConfig.class);
    verify(store).store(eq(repository), captor.capture());

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    assertThat(captor.getValue().getEncoding()).isEqualTo("UTF-8");
  }

  @Test
  void shouldFailWithInvalidEncoding() throws IOException, URISyntaxException {
    HgRepositoryConfigDto dto = new HgRepositoryConfigDto(Links.emptyLinks());
    dto.setEncoding("XA");

    MockHttpRequest request = MockHttpRequest.put("/v2/config/hg/hitchhiker/hog")
      .contentType(HgVndMediaType.REPO_CONFIG).content(objectMapper.writeValueAsBytes(dto));
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
  }

}
