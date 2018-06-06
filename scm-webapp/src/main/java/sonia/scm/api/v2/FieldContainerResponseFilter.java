package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Optional;

@Provider
@Priority(Priorities.USER)
public class FieldContainerResponseFilter implements ContainerResponseFilter {

  private static final String PARAMETER_FIELDS = "fields";
  private static final String FIELD_SEPARATOR = ",";

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    Optional<JsonNode> entity = getJsonEntity(responseContext);
    if (entity.isPresent()) {
      List<String> fields = extractFieldsFrom(requestContext);
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

  private List<String> extractFieldsFrom(ContainerRequestContext requestContext) {
    List<String> fields = Lists.newArrayList();

    List<String> fieldParameters = getFieldParameterFrom(requestContext);
    if (fieldParameters != null && !fieldParameters.isEmpty()) {
      for (String fieldParameter : fieldParameters) {
        appendFieldsFromParameter(fields, fieldParameter);
      }
    }

    return fields;
  }

  private List<String> getFieldParameterFrom(ContainerRequestContext requestContext) {
    MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
    return queryParameters.get(PARAMETER_FIELDS);
  }

  private void appendFieldsFromParameter(List<String> fields, String fieldParameter) {
    for (String field : fieldParameter.split(FIELD_SEPARATOR)) {
      fields.add(field);
    }
  }

}
