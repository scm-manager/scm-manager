/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.MediaType;
import org.junit.Test;

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
