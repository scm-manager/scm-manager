package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.web.JsonEnricher;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

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

  @Before
  public void setUpObjectUnderTest() throws URISyntaxException {
    this.enrichers = new HashSet<>();
    filter = new JsonMarshallingResponseFilter(mapper, enrichers);

    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getRequestUri()).thenReturn(new URI("https://www.scm-manager.org/scm/api/v2/repositories"));
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
    enrichers.add(context -> {
      JsonNode node = context.getResponseEntity();
      if (node.isObject()) {
        ((ObjectNode)node).put("version", 2);
      }
    });

    when(responseContext.hasEntity()).thenReturn(Boolean.TRUE);
    when(responseContext.getEntity()).thenReturn(new JsonMarshallingResponseFilterTest.Sample("one-two-three"));
    when(responseContext.getMediaType()).thenReturn(MediaType.valueOf(VndMediaType.USER));

    filter.filter(requestContext, responseContext);

    verify(responseContext).setEntity(jsonNodeCaptor.capture());

    JsonNode node = jsonNodeCaptor.getValue();
    assertEquals(2, node.get("version").asInt());
  }

  @Test
  public void testFilterWithoutEntity() {
    filter.filter(requestContext, responseContext);
    verify(responseContext, never()).setEntity(Mockito.anyObject());
  }

  @Test
  public void testFilterWithNonVndEntity() {
    when(responseContext.hasEntity()).thenReturn(Boolean.TRUE);
    when(responseContext.getEntity()).thenReturn(new JsonMarshallingResponseFilterTest.Sample("one-two-three"));
    when(responseContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    filter.filter(requestContext, responseContext);
    verify(responseContext, never()).setEntity(Mockito.anyObject());
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
