package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.SvnConfig;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class SvnConfigInIndexResource extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public SvnConfigInIndexResource(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context) && ConfigurationPermissions.read(SvnConfig.PERMISSION).isPermitted()) {
      String svnConfigUrl = new LinkBuilder(scmPathInfoStore.get().get(), SvnConfigResource.class)
        .method("get")
        .parameters()
        .href();

      JsonNode svnConfigRefNode = createObject(singletonMap("href", value(svnConfigUrl)));

      addPropertyNode(context.getResponseEntity().get("_links"), "svnConfig", svnConfigRefNode);
    }
  }
}
