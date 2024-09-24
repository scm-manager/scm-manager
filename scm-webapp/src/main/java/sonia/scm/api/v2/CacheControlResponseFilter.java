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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the Cache-Control: no-cache header to every api call. But only if non caching headers are set to the response.
 * The Cache-Control header should fix stale resources on ie.
 */
@Provider
public class CacheControlResponseFilter implements ContainerResponseFilter {

  private static final Logger LOG = LoggerFactory.getLogger(CacheControlResponseFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (shouldAppendCachingHeader(responseContext)) {
      LOG.trace("add no-cache header to response");
      responseContext.getHeaders().add("Cache-Control", "no-cache");
    }
  }

  private boolean shouldAppendCachingHeader(ContainerResponseContext responseContext) {
    return !hasAlreadyCacheControl(responseContext) && !isCacheable(responseContext);
  }

  private boolean hasAlreadyCacheControl(ContainerResponseContext responseContext) {
    return responseContext.getHeaders().containsKey("Cache-Control");
  }

  private boolean isCacheable(ContainerResponseContext responseContext) {
    return hasLastModifiedDate(responseContext) || hasEntityTag(responseContext);
  }

  private boolean hasEntityTag(ContainerResponseContext responseContext) {
    return responseContext.getEntityTag() != null;
  }

  private boolean hasLastModifiedDate(ContainerResponseContext responseContext) {
    return responseContext.getLastModified() != null;
  }
}
