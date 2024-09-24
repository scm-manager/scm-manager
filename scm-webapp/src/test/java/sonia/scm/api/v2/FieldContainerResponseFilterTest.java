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
