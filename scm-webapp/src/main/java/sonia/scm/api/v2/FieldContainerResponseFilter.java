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
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * <p>Post processor for REST requests filtering JSON responses when a {@value PARAMETER_FIELDS} query
 * parameter is provided. In this case, only the given fields will returned. It is possible to specify
 * paths for nested fields. Multiple fields have to be separated using {@value FIELD_SEPARATOR}.</p>
 * <p>This requires the {@link JsonMarshallingResponseFilter} to be processed first to create
 * the {@link JsonNode} tree.</p>
 */
@Provider
@Priority(ResponseFilterPriorities.FIELD_FILTER)
public class FieldContainerResponseFilter implements ContainerResponseFilter {

  private static final String PARAMETER_FIELDS = "fields";
  private static final String FIELD_SEPARATOR = ",";

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    Optional<JsonNode> entity = getJsonEntity(responseContext);
    if (entity.isPresent()) {
      Collection<String> fields = extractFieldsFrom(requestContext);
      if (!fields.isEmpty()) {
        JsonFilters.filterByFields(entity.get(), fields);
      }
    }
  }

  private Optional<JsonNode> getJsonEntity(ContainerResponseContext responseContext) {
    Object entity = responseContext.getEntity();
    if (isJsonEntity(entity)) {
      return Optional.of((JsonNode) entity);
    }
    return Optional.empty();
  }

  private boolean isJsonEntity(Object entity) {
    return entity instanceof JsonNode;
  }

  private Collection<String> extractFieldsFrom(ContainerRequestContext requestContext) {
    return getFieldParameterFrom(requestContext)
      .orElse(emptyList())
      .stream()
      .flatMap(p -> stream(p.split(FIELD_SEPARATOR)))
      .collect(Collectors.toList());
  }

  private Optional<Collection<String>> getFieldParameterFrom(ContainerRequestContext requestContext) {
    MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
    Collection<String> fieldParameters = queryParameters.get(PARAMETER_FIELDS);
    return ofNullable(fieldParameters);
  }
}
