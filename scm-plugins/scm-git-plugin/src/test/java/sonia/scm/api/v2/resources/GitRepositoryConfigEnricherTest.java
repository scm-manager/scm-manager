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
import com.google.common.io.Resources;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitRepositoryConfigEnricherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private GitRepositoryConfigEnricher linkEnricher;
  private JsonNode rootNode;
  @Mock
  private RepositoryManager manager;

  @BeforeEach
  void globalSetUp() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    Provider<ScmPathInfoStore> pathInfoStoreProvider = Providers.of(pathInfoStore);

    linkEnricher = new GitRepositoryConfigEnricher(pathInfoStoreProvider, objectMapper, manager);
  }

  @Nested
  class ForSingleRepository {
    @BeforeEach
    void setUp() throws IOException {
      URL resource = Resources.getResource("sonia/scm/repository/repository-001.json");
      rootNode = objectMapper.readTree(resource);

      when(manager.get(new NamespaceAndName("scmadmin", "web-resources"))).thenReturn(new Repository("id", "git", "scmadmin", "web-resources"));
    }

    @Test
    void shouldEnrichGitRepositories() {
      JsonEnricherContext context = new JsonEnricherContext(
        URI.create("/"),
        MediaType.valueOf(VndMediaType.REPOSITORY),
        rootNode
      );

      linkEnricher.enrich(context);

      String configLink = context.getResponseEntity()
        .get("_links")
        .get("configuration")
        .get("href")
        .asText();

      assertThat(configLink).isEqualTo("/v2/config/git/scmadmin/web-resources");
    }

    @Test
    void shouldNotEnrichOtherRepositories() {
      when(manager.get(new NamespaceAndName("scmadmin", "web-resources"))).thenReturn(new Repository("id", "hg", "scmadmin", "web-resources"));

      JsonEnricherContext context = new JsonEnricherContext(
        URI.create("/"),
        MediaType.valueOf(VndMediaType.REPOSITORY),
        rootNode
      );

      linkEnricher.enrich(context);

      JsonNode configLink = context.getResponseEntity()
        .get("_links")
        .get("configuration");

      assertThat(configLink).isNull();
    }
  }

  @Nested
  class ForRepositoryCollection {
    @BeforeEach
    void setUp() throws IOException {
      URL resource = Resources.getResource("sonia/scm/repository/repository-collection-001.json");
      rootNode = objectMapper.readTree(resource);

      when(manager.get(new NamespaceAndName("scmadmin", "web-resources"))).thenReturn(new Repository("id", "git", "scmadmin", "web-resources"));
    }

    @Test
    void shouldEnrichAllRepositories() {
      JsonEnricherContext context = new JsonEnricherContext(
        URI.create("/"),
        MediaType.valueOf(VndMediaType.REPOSITORY_COLLECTION),
        rootNode
      );

      linkEnricher.enrich(context);

      context.getResponseEntity()
        .get("_embedded")
        .withArray("repositories")
        .elements()
        .forEachRemaining(node -> {
          String configLink = node
            .get("_links")
            .get("configuration")
            .get("href")
            .asText();

          assertThat(configLink).isEqualTo("/v2/config/git/scmadmin/web-resources");
        });
    }
  }

  @Test
  void shouldNotModifyObjectsWithUnsupportedMediaType() throws IOException {
    URL resource = Resources.getResource("sonia/scm/repository/repository-001.json");
    rootNode = objectMapper.readTree(resource);
    JsonEnricherContext context = new JsonEnricherContext(
      URI.create("/"),
      MediaType.valueOf(VndMediaType.USER),
      rootNode
    );

    linkEnricher.enrich(context);

    boolean hasNewPullRequestLink = context.getResponseEntity()
      .get("_links")
      .has("configuration");

    assertThat(hasNewPullRequestLink).isFalse();
  }
}
