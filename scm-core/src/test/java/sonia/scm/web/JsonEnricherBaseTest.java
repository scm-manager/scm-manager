/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
