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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Slf4j
public class FileHistoryRootResource {

  private final RepositoryServiceFactory serviceFactory;

  private final FileHistoryCollectionToDtoMapper fileHistoryCollectionToDtoMapper;


  @Inject
  public FileHistoryRootResource(RepositoryServiceFactory serviceFactory, FileHistoryCollectionToDtoMapper fileHistoryCollectionToDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.fileHistoryCollectionToDtoMapper = fileHistoryCollectionToDtoMapper;
  }

  /**
   * Get all changesets related to the given file starting with the given revision
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @param revision  the revision
   * @param path      the path of the file
   * @param page      pagination
   * @param pageSize  pagination
   * @return all changesets related to the given file starting with the given revision
   * @throws IOException                 on io error
   */
  @GET
  @Path("{revision}/{path: .*}")
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @Operation(summary = "Changesets to given file", description = "Get all changesets related to the given file starting with the given revision.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.CHANGESET_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the changeset")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no changesets available in the repository",
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
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name,
                         @PathParam("revision") String revision,
                         @PathParam("path") String path,
                         @DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("10") @QueryParam("pageSize") int pageSize) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      log.info("Get changesets of the file {} and revision {}", path, revision);
      Repository repository = repositoryService.getRepository();
      ChangesetPagingResult changesets = new PagedLogCommandBuilder(repositoryService)
        .page(page)
        .pageSize(pageSize)
        .create()
        .setPath(path)
        .setStartChangeset(revision)
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        return Response.ok(fileHistoryCollectionToDtoMapper.map(page, pageSize, pageResult, repository, revision, path)).build();
      } else {
        String message = String.format("for the revision %s and the file %s there are no changesets", revision, path);
        log.error(message);
        throw notFound(entity("Path", path).in("revision", revision).in(namespaceAndName));
      }
    }
  }
}
