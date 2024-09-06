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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryPathCollector;
import sonia.scm.repository.RepositoryPaths;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

public class RepositoryPathsResource {

  private final RepositoryPathCollector collector;

  @Inject
  public RepositoryPathsResource(RepositoryPathCollector collector) {
    this.collector = collector;
  }

  /**
   * Returns all file paths for the given revision in the repository
   *
   * @param namespace the namespace of the repository
   * @param name      the name of the repository
   * @param revision  the revision
   */
  @GET
  @Path("{revision}")
  @Produces(VndMediaType.REPOSITORY_PATHS)
  @Operation(summary = "File paths by revision", description = "Returns all file paths for the given revision in the repository.", tags = "Repository")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the repository")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified name available in the namespace",
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
    ))
  public RepositoryPathsDto collect(
    @Context UriInfo uriInfo,
    @PathParam("namespace") String namespace,
    @PathParam("name") String name,
    @PathParam("revision") String revision) throws IOException
  {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    RepositoryPaths paths = collector.collect(namespaceAndName, revision);
    return map(uriInfo, paths);
  }

  private RepositoryPathsDto map(UriInfo uriInfo, RepositoryPaths paths) {
    RepositoryPathsDto dto = new RepositoryPathsDto(createLinks(uriInfo));
    dto.setRevision(paths.getRevision());
    dto.setPaths(paths.getPaths());
    return dto;
  }

  private Links createLinks(UriInfo uriInfo) {
    return Links.linkingTo().self(uriInfo.getAbsolutePath().toASCIIString()).build();
  }
}
