package sonia.scm.api.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Adds the Cache-Control: no-cache header to every api call. But only if non caching headers are set to the response.
 * The Cache-Control header should fix stale resources on ie.
 */
@Provider
public class CacheControlResponseFilter implements ContainerResponseFilter {

  private static final Logger LOG = LoggerFactory.getLogger(CacheControlResponseFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (!isCacheable(responseContext)) {
      LOG.trace("add no-cache header to response");
      responseContext.getHeaders().add("Cache-Control", "no-cache");
    }
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
