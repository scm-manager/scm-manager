package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.plugin.Extension;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.REPOSITORY;

@Extension
public class GitRepositoryConfigEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public GitRepositoryConfigEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(REPOSITORY, context)) {
      JsonNode repositoryNode = context.getResponseEntity();
      String namespace = repositoryNode.get("namespace").asText();
      String name = repositoryNode.get("name").asText();

      String newPullRequest = new LinkBuilder(scmPathInfoStore.get().get(), GitConfigResource.class)
        .method("getRepositoryConfig")
        .parameters(namespace, name)
        .href();

      JsonNode newPullRequestNode = createObject(singletonMap("href", value(newPullRequest)));

      addPropertyNode(repositoryNode.get("_links"), "configuration", newPullRequestNode);
    }
  }
}
