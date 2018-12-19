package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.ws.rs.core.MediaType;
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
  void globalSetUp() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));

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
