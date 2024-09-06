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

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

@Provider
public class UriInfoFilter implements ContainerRequestFilter {

  private final jakarta.inject.Provider<ScmPathInfoStore> storeProvider;

  @Inject
  public UriInfoFilter(jakarta.inject.Provider<ScmPathInfoStore> storeProvider) {
    this.storeProvider = storeProvider;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    UriInfo uriInfo = requestContext.getUriInfo();
    storeProvider.get().set(uriInfo::getBaseUri);
  }
}
