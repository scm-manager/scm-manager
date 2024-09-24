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
 *  RESTful web service resource to manage repository roles.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Repository role", description = "Repository role related endpoints")
})
@Path(RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2)
public class RepositoryRoleRootResource {

  static final String REPOSITORY_ROLES_PATH_V2 = "v2/repositoryRoles/";

  private final Provider<RepositoryRoleCollectionResource> repositoryRoleCollectionResource;
  private final Provider<RepositoryRoleResource> repositoryRoleResource;

  @Inject
  public RepositoryRoleRootResource(Provider<RepositoryRoleCollectionResource> repositoryRoleCollectionResource,
                                    Provider<RepositoryRoleResource> repositoryRoleResource) {
    this.repositoryRoleCollectionResource = repositoryRoleCollectionResource;
    this.repositoryRoleResource = repositoryRoleResource;
  }

  @Path("")
  public RepositoryRoleCollectionResource getRepositoryRoleCollectionResource() {
    return repositoryRoleCollectionResource.get();
  }

  @Path("{name}")
  public RepositoryRoleResource getRepositoryRoleResource() {
    return repositoryRoleResource.get();
  }
}
