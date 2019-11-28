package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonEnricherBaseTest {

  private ObjectMapper objectMapper = new ObjectMapper();
  private TestJsonEnricher enricher = new TestJsonEnricher(objectMapper);

  @Test
  public void testResultHasMediaType() {
    JsonEnricherContext context = new JsonEnricherContext(null, MediaType.APPLICATION_JSON_TYPE, null);

    assertThat(enricher.resultHasMediaType(MediaType.APPLICATION_JSON, context)).isTrue();
    assertThat(enricher.resultHasMediaType(MediaType.APPLICATION_XML, context)).isFalse();
  }

  @Test
  public void testResultHasMediaTypeWithCamelCaseMediaType() {
    String mediaType = "application/hitchhikersGuideToTheGalaxy";
    JsonEnricherContext context = new JsonEnricherContext(null, MediaType.valueOf(mediaType), null);

    assertThat(enricher.resultHasMediaType(mediaType, context)).isTrue();
  }

  @Test
  public void testAppendLink() {
    ObjectNode root = objectMapper.createObjectNode();
    ObjectNode links = objectMapper.createObjectNode();
    root.set("_links", links);
    JsonEnricherContext context = new JsonEnricherContext(null, MediaType.APPLICATION_JSON_TYPE, root);
    enricher.enrich(context);

    assertThat(links.get("awesome").get("href").asText()).isEqualTo("/my/awesome/link");
  }

  private static class TestJsonEnricher extends JsonEnricherBase {

    public TestJsonEnricher(ObjectMapper objectMapper) {
      super(objectMapper);
    }

    @Override
    public void enrich(JsonEnricherContext context) {
      JsonNode gitConfigRefNode = createObject(singletonMap("href", value("/my/awesome/link")));

      addPropertyNode(context.getResponseEntity().get("_links"), "awesome", gitConfigRefNode);
    }
  }

}
