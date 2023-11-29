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
