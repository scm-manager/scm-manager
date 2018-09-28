package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.web.JsonEnricher;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
public class GitConfigInIndexResource implements JsonEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final ObjectMapper objectMapper;

  @Inject
  public GitConfigInIndexResource(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.objectMapper = objectMapper;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (isIndexRequest(context) && ConfigurationPermissions.list().isPermitted()) {
      String gitConfigUrl = new LinkBuilder(scmPathInfoStore.get().get(), GitConfigResource.class)
        .method("get")
        .parameters()
        .href();

      ObjectNode gitConfigRefNode = objectMapper.createObjectNode();
      gitConfigRefNode.set("href", objectMapper.convertValue(gitConfigUrl, JsonNode.class));

      ((ObjectNode) context.getResponseEntity().get("_links")).put("gitConfig", gitConfigRefNode);
    }
  }

  private boolean isIndexRequest(JsonEnricherContext context) {
    return VndMediaType.INDEX.equals(context.getResponseMediaType().toString());
  }
}
