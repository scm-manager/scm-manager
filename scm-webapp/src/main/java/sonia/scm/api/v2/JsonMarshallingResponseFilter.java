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
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import sonia.scm.web.JsonEnricher;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import java.util.Set;

/**
 * Transforms JSON REST responses to {@link JsonNode} trees to support further post processing
 * and processes all registered plugins for the {@link JsonEnricher} extension point.
 */
@Provider
@Priority(ResponseFilterPriorities.JSON_MARSHALLING)
public class JsonMarshallingResponseFilter implements ContainerResponseFilter {

  private static final MediaType ERROR_MEDIA_TYPE = MediaType.valueOf(VndMediaType.ERROR_TYPE);

  private final ObjectMapper objectMapper;
  private final Set<JsonEnricher> enrichers;

  @Inject
  public JsonMarshallingResponseFilter(ObjectMapper objectMapper, Set<JsonEnricher> enrichers) {
    this.objectMapper = objectMapper;
    this.enrichers = enrichers;
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (isError(responseContext)) {
      return;
    }
    if (hasVndEntity(responseContext)) {
      JsonNode node = getJsonEntity(responseContext);
      callEnrichers(requestContext, responseContext, node);
      responseContext.setEntity(node);
    }
  }

  private boolean isError(ContainerResponseContext responseContext) {
    return ERROR_MEDIA_TYPE.equals(responseContext.getMediaType());
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
