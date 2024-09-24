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
