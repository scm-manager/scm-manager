package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * <p>Post processor for rest requests filtering json responses when a {@value PARAMETER_FIELDS} query
 * parameter is provided. In this case, only the given fields will returned. It is possible, to specify
 * paths for nested fields. Multiple fields have to be devided using {@value FIELD_SEPARATOR}.</p>
 * <p>This requires the {@link JsonMarshallingResponseFilter} to be processed first to create
 * the {@link JsonNode} tree.</p>
 */
@Provider
@Priority(Priorities.USER)
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
