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

package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.web.JsonEnricher;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsonMarshallingResponseFilterTest {

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private ContainerResponseContext responseContext;

  @Mock
  private UriInfo uriInfo;

  @Captor
  private ArgumentCaptor<JsonNode> jsonNodeCaptor;

  private final ObjectMapper mapper = new ObjectMapper();

  private Set<JsonEnricher> enrichers;

  private JsonMarshallingResponseFilter filter;

  private URI expectedUri;

  @Before
  public void setUpObjectUnderTest() throws URISyntaxException {
    this.enrichers = new HashSet<>();
    filter = new JsonMarshallingResponseFilter(mapper, enrichers);

    expectedUri = new URI("https://www.scm-manager.org/scm/api/v2/repositories");

    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getRequestUri()).thenReturn(expectedUri);
  }

  @Test
  public void testFilter() {
    when(responseContext.hasEntity()).thenReturn(Boolean.TRUE);
    when(responseContext.getEntity()).thenReturn(new JsonMarshallingResponseFilterTest.Sample("one-two-three"));
    when(responseContext.getMediaType()).thenReturn(MediaType.valueOf(VndMediaType.USER));

    filter.filter(requestContext, responseContext);

    verify(responseContext).setEntity(jsonNodeCaptor.capture());

    JsonNode node = jsonNodeCaptor.getValue();
    assertEquals("one-two-three", node.get("value").asText());
  }

  @Test
  public void testFilterWithEnricher() {
    Sample expectedEntity = new Sample("one-two-three");
    MediaType expectedMediaType = MediaType.valueOf(VndMediaType.USER);

    when(responseContext.hasEntity()).thenReturn(Boolean.TRUE);
    when(responseContext.getEntity()).thenReturn(expectedEntity);
    when(responseContext.getMediaType()).thenReturn(expectedMediaType);

    enrichers.add(context -> {
      JsonNode node = context.getResponseEntity();

      assertEquals(mapper.valueToTree(expectedEntity), node);
      assertEquals(expectedUri, context.getRequestUri());
      assertEquals(expectedMediaType, context.getResponseMediaType());

      if (node.isObject()) {
        ((ObjectNode)node).put("version", 2);
      }
    });

    filter.filter(requestContext, responseContext);

    verify(responseContext).setEntity(jsonNodeCaptor.capture());

    JsonNode node = jsonNodeCaptor.getValue();
    assertEquals(2, node.get("version").asInt());
  }

  @Test
  public void testFilterWithoutEntity() {
    filter.filter(requestContext, responseContext);
    verify(responseContext, never()).setEntity(any());
  }

  @Test
  public void testFilterWithNonVndEntity() {
    when(responseContext.hasEntity()).thenReturn(Boolean.TRUE);
    when(responseContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    filter.filter(requestContext, responseContext);
    verify(responseContext, never()).setEntity(any());
  }

  public static class Sample {

    private String value;

    public Sample(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
