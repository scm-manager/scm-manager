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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.web.VndMediaType;

public class RepositoryTypeResource {

  private RepositoryManager repositoryManager;
  private RepositoryTypeToRepositoryTypeDtoMapper mapper;

  @Inject
  public RepositoryTypeResource(RepositoryManager repositoryManager, RepositoryTypeToRepositoryTypeDtoMapper mapper) {
    this.repositoryManager = repositoryManager;
    this.mapper = mapper;
  }

  /**
   * Returns the specified repository type.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param name of the requested repository type
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_TYPE)
  @Operation(summary = "Get single repository type", description = "Returns the specified repository type for the given name.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_TYPE,
      schema = @Schema(implementation = RepositoryTypeDto.class)
    )
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository type with the specified name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response get(@PathParam("name") String name) {
    for (RepositoryType type : repositoryManager.getConfiguredTypes()) {
      if (name.equalsIgnoreCase(type.getName())) {
        return Response.ok(mapper.map(type)).build();
      }
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

}
