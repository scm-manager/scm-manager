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

import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.web.VndMediaType;

/**
 * RESTful Web Service Resource to get available repository verbs.
 */
@Path(RepositoryVerbResource.PATH)
public class RepositoryVerbResource {

  static final String PATH = "v2/repositoryVerbs/";

  private final RepositoryPermissionProvider repositoryPermissionProvider;
  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryVerbResource(RepositoryPermissionProvider repositoryPermissionProvider, ResourceLinks resourceLinks) {
    this.repositoryPermissionProvider = repositoryPermissionProvider;
    this.resourceLinks = resourceLinks;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_VERB_COLLECTION)
  @Operation(summary = "List of repository verbs", description = "Returns all repository-specific permissions.", hidden = true)
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_VERB_COLLECTION,
      schema = @Schema(implementation = RepositoryVerbsDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public RepositoryVerbsDto getAll() {
    return new RepositoryVerbsDto(
      Links.linkingTo().self(resourceLinks.repositoryVerbs().self()).build(),
      repositoryPermissionProvider.availableVerbs()
    );
  }
}
