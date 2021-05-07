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
