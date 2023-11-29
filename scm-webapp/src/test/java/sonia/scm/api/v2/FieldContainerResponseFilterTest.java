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
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    JsonNode node = applyEntity("filter-test-nested");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterWithMultiple() throws IOException {
    applyFields("one", "five");
    JsonNode node = applyEntity("filter-test-nested");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1,\"five\":5}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterCommaSeparated() throws IOException {
    applyFields("one,five");
    JsonNode node = applyEntity("filter-test-nested");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1,\"five\":5}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterEmpty() throws IOException {
    applyFields();
    JsonNode node = applyEntity("filter-test-nested");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1,\"two\":{\"three\":3,\"four\":4},\"five\":5}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterNotSet() throws IOException {
    applyFields((List) null);
    JsonNode node = applyEntity("filter-test-nested");

    filter.filter(requestContext, responseContext);

    assertEquals("{\"one\":1,\"two\":{\"three\":3,\"four\":4},\"five\":5}", objectMapper.writeValueAsString(node));
  }

  private void applyFields(String... fields) {
    ArrayList<String> fieldList = Lists.newArrayList(fields);
    applyFields(fieldList);
  }

  private void applyFields(List<String> fieldList) {
    UriInfo info = mock(UriInfo.class);
    MultivaluedMap<String,String> queryParameters = mock(MultivaluedMap.class);
    when(queryParameters.get("fields")).thenReturn(fieldList);
    when(info.getQueryParameters()).thenReturn(queryParameters);
    when(requestContext.getUriInfo()).thenReturn(info);
  }

  private JsonNode applyEntity(String name) throws IOException {
    JsonNode node = readJson(name);
    when(responseContext.getEntity()).thenReturn(node);
    return node;
  }

  private JsonNode readJson(String name) throws IOException {
    URL resource = Resources.getResource("sonia/scm/api/v2/" + name + ".json");
    return objectMapper.readTree(resource);
  }
}
