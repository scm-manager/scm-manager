package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.util.Providers;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.MediaType;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini")
public class HgConfigInIndexResourceTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ObjectNode root = objectMapper.createObjectNode();
  private final HgConfigInIndexResource hgConfigInIndexResource;

  public HgConfigInIndexResourceTest() {
    root.put("_links", objectMapper.createObjectNode());
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    hgConfigInIndexResource = new HgConfigInIndexResource(Providers.of(pathInfoStore), objectMapper);
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void admin() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    hgConfigInIndexResource.enrich(context);

    assertEquals("/v2/config/hg", root.get("_links").get("hgConfig").get("href").asText());
  }

  @Test
  @SubjectAware(username = "readOnly", password = "secret")
  public void user() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    hgConfigInIndexResource.enrich(context);

    assertFalse(root.get("_links").iterator().hasNext());
  }

  @Test
  public void anonymous() {
    JsonEnricherContext context = new JsonEnricherContext(URI.create("/index"), MediaType.valueOf(VndMediaType.INDEX), root);

    hgConfigInIndexResource.enrich(context);

    assertFalse(root.get("_links").iterator().hasNext());
  }
}
