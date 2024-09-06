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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;

/**
 * RESTful Web Service Resource to manage repositories.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Repository", description = "Repository related endpoints")
})
@Path(RepositoryRootResource.REPOSITORIES_PATH_V2)
public class RepositoryRootResource {
  static final String REPOSITORIES_PATH_V2 = "v2/repositories/";

  private final Provider<RepositoryResource> repositoryResource;
  private final Provider<RepositoryCollectionResource> repositoryCollectionResource;
  private final Provider<RepositoryImportResource> repositoryImportResource;

  @Inject
  public RepositoryRootResource(Provider<RepositoryResource> repositoryResource, Provider<RepositoryCollectionResource> repositoryCollectionResource, Provider<RepositoryImportResource> repositoryImportResource) {
    this.repositoryResource = repositoryResource;
    this.repositoryCollectionResource = repositoryCollectionResource;
    this.repositoryImportResource = repositoryImportResource;
  }

  @Path("{namespace}/{name}")
  public RepositoryResource getRepositoryResource() {
    return repositoryResource.get();
  }

  @Path("")
  public RepositoryCollectionResource getRepositoryCollectionResource() {
    return repositoryCollectionResource.get();
  }

  @Path("import")
  public RepositoryImportResource getRepositoryImportResource() {
    return repositoryImportResource.get();
  }
}
