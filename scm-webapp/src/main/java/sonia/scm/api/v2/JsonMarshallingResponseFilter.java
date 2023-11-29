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
