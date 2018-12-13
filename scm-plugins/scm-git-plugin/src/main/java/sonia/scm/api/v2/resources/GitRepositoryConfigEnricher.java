package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.REPOSITORY;
import static sonia.scm.web.VndMediaType.REPOSITORY_COLLECTION;

@Extension
public class GitRepositoryConfigEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final RepositoryManager manager;

  @Inject
  public GitRepositoryConfigEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper, RepositoryManager manager) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
    this.manager = manager;
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

    if (GitRepositoryHandler.TYPE_NAME.equals(manager.get(new NamespaceAndName(namespace, name)).getType())) {
      String repositoryConfigLink = new LinkBuilder(scmPathInfoStore.get().get(), GitConfigResource.class)
        .method("getRepositoryConfig")
        .parameters(namespace, name)
        .href();

      JsonNode newPullRequestNode = createObject(singletonMap("href", value(repositoryConfigLink)));

      addPropertyNode(repositoryNode.get("_links"), "configuration", newPullRequestNode);
    }
  }
}
