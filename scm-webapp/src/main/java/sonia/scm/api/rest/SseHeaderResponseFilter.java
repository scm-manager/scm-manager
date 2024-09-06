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

package sonia.scm.api.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import sonia.scm.sse.SseResponse;

import java.io.IOException;

@Provider
@SseResponse
public class SseHeaderResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    cacheControl(headers);
    avoidNginxBuffering(headers);
  }

  private void avoidNginxBuffering(MultivaluedMap<String, Object> headers) {
    headers.add("X-Accel-Buffering", "no");
  }

  private void cacheControl(MultivaluedMap<String, Object> headers) {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);

    headers.remove("Cache-Control");
    headers.add("Cache-Control", cacheControl);
  }
}
