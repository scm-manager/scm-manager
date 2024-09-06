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
import sonia.scm.repository.Modifications;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

public class ModificationsRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final ModificationsToDtoMapper modificationsToDtoMapper;

  @Inject
  public ModificationsRootResource(RepositoryServiceFactory serviceFactory, ModificationsToDtoMapper modificationsToDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.modificationsToDtoMapper = modificationsToDtoMapper;
  }

  /**
   * Get the file modifications related to a revision.
   * file modifications are for example: Modified, Added or Removed.
   */
  @GET
  @Path("{revision}")
  @Produces(VndMediaType.MODIFICATIONS)
  @Operation(summary = "File modifications", description = "Get the file modifications related to a revision.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.MODIFICATIONS,
      schema = @Schema(implementation = ModificationsDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the modifications")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no changeset with the specified id is available in the repository",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Modifications modifications = repositoryService.getModificationsCommand()
        .revision(revision)
        .getModifications();
      ModificationsDto output = modificationsToDtoMapper.map(modifications, repositoryService.getRepository());
      if (modifications != null ) {
        return Response.ok(output).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    }
  }
}
