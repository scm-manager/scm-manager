package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HgConfig;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class HgConfigInIndexResource extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public HgConfigInIndexResource(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context) && ConfigurationPermissions.read(HgConfig.PERMISSION).isPermitted()) {
      String hgConfigUrl = new LinkBuilder(scmPathInfoStore.get().get(), HgConfigResource.class)
        .method("get")
        .parameters()
        .href();

      JsonNode hgConfigRefNode = createObject(singletonMap("href", value(hgConfigUrl)));

      addPropertyNode(context.getResponseEntity().get("_links"), "hgConfig", hgConfigRefNode);
    }
  }
}
