package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldContainerResponseFilterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private ContainerResponseContext responseContext;

  private FieldContainerResponseFilter filter = new FieldContainerResponseFilter();

  @Test
  public void testFilter() throws IOException {
    applyFields("one");
    JsonNode node = applyEntity("filter-test-002");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterWithMultiple() throws IOException {
    applyFields("one", "five");
    JsonNode node = applyEntity("filter-test-002");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1,\"five\":5}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterCommaSeparated() throws IOException {
    applyFields("one,five");
    JsonNode node = applyEntity("filter-test-002");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1,\"five\":5}", objectMapper.writeValueAsString(node));
  }

  private void applyFields(String... fields) {
    UriInfo info = mock(UriInfo.class);
    MultivaluedMap<String,String> queryParameters = mock(MultivaluedMap.class);
    when(queryParameters.get("fields")).thenReturn(Lists.newArrayList(fields));
    when(info.getQueryParameters()).thenReturn(queryParameters);
    when(requestContext.getUriInfo()).thenReturn(info);
  }

  private JsonNode applyEntity(String name) throws IOException {
    JsonNode node = readJson(name);
    when(responseContext.hasEntity()).thenReturn(Boolean.TRUE);
    when(responseContext.getEntity()).thenReturn(node);
    return node;
  }

  private JsonNode readJson(String name) throws IOException {
    URL resource = Resources.getResource("sonia/scm/api/v2/" + name + ".json");
    return objectMapper.readTree(resource);
  }
}
