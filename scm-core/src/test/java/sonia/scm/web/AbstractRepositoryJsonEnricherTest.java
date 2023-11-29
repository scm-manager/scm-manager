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

package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AbstractRepositoryJsonEnricherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private AbstractRepositoryJsonEnricher linkEnricher;
  private JsonNode rootNode;

  @BeforeEach
  void setUpPathInfoStore() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
  }

  @Nested
  class WithWorkingEnricher {

    @BeforeEach
    void setUpEnricher() {
      linkEnricher = new AbstractRepositoryJsonEnricher(objectMapper) {
        @Override
        protected void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name) {
          addLink(repositoryNode, "new-link", "/somewhere");
        }
      };
    }

    @Test
    void shouldEnrichRepositories() throws IOException {
      URL resource = Resources.getResource("sonia/scm/repository/repository-001.json");
      rootNode = objectMapper.readTree(resource);

      JsonEnricherContext context = new JsonEnricherContext(
        URI.create("/"),
        MediaType.valueOf(VndMediaType.REPOSITORY),
        rootNode
      );

      linkEnricher.enrich(context);

      String configLink = context.getResponseEntity()
        .get("_links")
        .get("new-link")
        .get("href")
        .asText();

      assertThat(configLink).isEqualTo("/somewhere");
    }

    @Test
    void shouldEnrichAllRepositories() throws IOException {
      URL resource = Resources.getResource("sonia/scm/repository/repository-collection-001.json");
      rootNode = objectMapper.readTree(resource);

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
            .get("new-link")
            .get("href")
            .asText();

          assertThat(configLink).isEqualTo("/somewhere");
        });
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
        .has("new-link");

      assertThat(hasNewPullRequestLink).isFalse();
    }
  }

  @Test
  void shouldHandleFailingEnricher() throws IOException {
    linkEnricher = new AbstractRepositoryJsonEnricher(objectMapper) {
      @Override
      protected void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name) {
        throw new NullPointerException();
      }
    };
    URL resource = Resources.getResource("sonia/scm/repository/repository-001.json");
    rootNode = objectMapper.readTree(resource);

    JsonEnricherContext context = new JsonEnricherContext(
      URI.create("/"),
      MediaType.valueOf(VndMediaType.REPOSITORY),
      rootNode
    );

    linkEnricher.enrich(context);

    // no exception has been thrown
  }
}
