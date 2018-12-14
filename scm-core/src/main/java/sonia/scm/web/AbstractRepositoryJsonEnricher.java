package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.REPOSITORY;
import static sonia.scm.web.VndMediaType.REPOSITORY_COLLECTION;

public abstract class AbstractRepositoryJsonEnricher extends JsonEnricherBase {

  public AbstractRepositoryJsonEnricher(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(REPOSITORY, context)) {
      JsonNode repositoryNode = context.getResponseEntity();
      enrichRepositoryNode(repositoryNode);
    } else if (resultHasMediaType(REPOSITORY_COLLECTION, context)) {
      JsonNode repositoryCollectionNode = context.getResponseEntity().get("_embedded").withArray("repositories");
      repositoryCollectionNode.elements().forEachRemaining(this::enrichRepositoryNode);
    }
  }

  private void enrichRepositoryNode(JsonNode repositoryNode) {
    String namespace = repositoryNode.get("namespace").asText();
    String name = repositoryNode.get("name").asText();

    enrichRepositoryNode(repositoryNode, namespace, name);
  }

  protected abstract void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name);

  protected void addLink(JsonNode repositoryNode, String linkName, String link) {
    JsonNode newPullRequestNode = createObject(singletonMap("href", value(link)));
    addPropertyNode(repositoryNode.get("_links"), linkName, newPullRequestNode);
  }
}
