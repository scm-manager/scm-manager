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

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;

/**
 * RESTful Web Service Resource to get available repository types.
 */
@Path(RepositoryTypeRootResource.PATH)
public class RepositoryTypeRootResource {

  static final String PATH = "v2/repositoryTypes/";

  private Provider<RepositoryTypeCollectionResource> collectionResourceProvider;
  private Provider<RepositoryTypeResource> resourceProvider;

  @Inject
  public RepositoryTypeRootResource(Provider<RepositoryTypeCollectionResource> collectionResourceProvider, Provider<RepositoryTypeResource> resourceProvider) {
    this.collectionResourceProvider = collectionResourceProvider;
    this.resourceProvider = resourceProvider;
  }

  @Path("")
  public RepositoryTypeCollectionResource getRepositoryTypeCollectionResource() {
    return collectionResourceProvider.get();
  }

  @Path("{name}")
  public RepositoryTypeResource getRepositoryTypeResource() {
    return resourceProvider.get();
  }


}
