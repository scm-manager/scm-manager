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
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

public class AnnotateResource {

  private final RepositoryServiceFactory serviceFactory;
  private final BlameResultToBlameDtoMapper mapper;

  @Inject
  public AnnotateResource(RepositoryServiceFactory serviceFactory, BlameResultToBlameDtoMapper mapper) {
    this.serviceFactory = serviceFactory;
    this.mapper = mapper;
  }

  /**
   * Returns the content of a file with additional information for each line regarding the last commit that
   * changed this line: The revision, the author, the date, and the description of the commit.
   *
   * @param namespace the namespace of the repository
   * @param name      the name of the repository
   * @param revision  the revision
   * @param path      The path of the file
   */
  @GET
  @Path("{revision}/{path: .*}")
  @Produces(VndMediaType.ANNOTATE)
  @Operation(summary = "File content by revision", description = "Returns the annotated file for the given revision in the repository.", tags = "Repository")
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
  public BlameDto annotate(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name,
    @PathParam("revision") String revision,
    @PathParam("path") String path
  ) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      return mapper.map(repositoryService.getBlameCommand().setRevision(revision).getBlameResult(path), namespaceAndName, revision, path);
    }
  }
}
