package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.web.JsonEnricher;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * Transforms JSON REST responses to {@link JsonNode} trees to support further post processing
 * and processes all registered plugins for the {@link JsonEnricher} extension point.
 */
@Provider
@Priority(ResponseFilterPriorities.JSON_MARSHALLING)
public class JsonMarshallingResponseFilter implements ContainerResponseFilter {

  private final ObjectMapper objectMapper;
  private final Set<JsonEnricher> enrichers;

  @Inject
  public JsonMarshallingResponseFilter(ObjectMapper objectMapper, Set<JsonEnricher> enrichers) {
    this.objectMapper = objectMapper;
    this.enrichers = enrichers;
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (hasVndEntity(responseContext)) {
      JsonNode node = getJsonEntity(responseContext);
      callEnrichers(requestContext, responseContext, node);
      responseContext.setEntity(node);
    }
  }

  private void callEnrichers(ContainerRequestContext requestContext, ContainerResponseContext responseContext, JsonNode node) {
    JsonEnricherContext context = new JsonEnricherContext(
      requestContext.getUriInfo().getRequestUri(),
      responseContext.getMediaType(),
      node
    );

    enrichers.forEach(enricher -> enricher.enrich(context));
  }

  private JsonNode getJsonEntity(ContainerResponseContext responseContext) {
    Object entity = responseContext.getEntity();
    return objectMapper.valueToTree(entity);
  }

  private boolean hasVndEntity(ContainerResponseContext responseContext) {
    return responseContext.hasEntity() && VndMediaType.isVndType(responseContext.getMediaType());
  }
}
