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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.ConflictException;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.RevertCommandBuilder;
import sonia.scm.repository.api.RevertCommandResult;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;


@Slf4j
public class ChangesetRootResource {

  private final RepositoryServiceFactory serviceFactory;

  private final ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper;

  private final ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper;

  private final ResourceLinks resourceLinks;

  @Inject
  ChangesetRootResource(RepositoryServiceFactory serviceFactory, ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper, ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    this.serviceFactory = serviceFactory;
    this.changesetCollectionToDtoMapper = changesetCollectionToDtoMapper;
    this.changesetToChangesetDtoMapper = changesetToChangesetDtoMapper;
    this.resourceLinks = resourceLinks;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @Operation(summary = "Collection of changesets", description = "Returns a collection of changesets.", tags = "Repository")
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
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("10") @QueryParam("pageSize") int pageSize) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      ChangesetPagingResult changesets = new PagedLogCommandBuilder(repositoryService)
        .page(page)
        .pageSize(pageSize)
        .create()
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        if (changesets.getBranchName() != null) {
          return Response.ok(changesetCollectionToDtoMapper.map(page, pageSize, pageResult, repository, changesets.getBranchName())).build();
        } else {
          return Response.ok(changesetCollectionToDtoMapper.map(page, pageSize, pageResult, repository)).build();
        }
      } else {
        return Response.ok().build();
      }
    }
  }

  @GET
  @Path("{id}")
  @Produces(VndMediaType.CHANGESET)
  @Operation(summary = "Specific changeset", description = "Returns a specific changeset.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.CHANGESET,
      schema = @Schema(implementation = ChangesetDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the changeset")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no changeset with the specified id is available in the repository",
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
  public ChangesetDto get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("id") String id) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      Changeset changeset = repositoryService.getLogCommand().getChangeset(id);
      if (changeset == null) {
        throw notFound(entity(Changeset.class, id).in(repository));
      }
      return changesetToChangesetDtoMapper.map(changeset, repository);
    }
  }

  @POST
  @Path("{id}/revert")
  @Operation(summary = "Revert changeset", description = "Reverts the changes of a single changeset.", tags = "Repository")
  @Consumes(VndMediaType.REVERT)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(
    responseCode = "201",
    description = "success",
    content = @Content(
      mediaType = "text/plain",
      schema = @Schema(implementation = ChangesetDto.class)
    )
  )
  @ApiResponse(
    responseCode = "400",
    description = "bad request, no parent for the changeset available or multiple parents",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to revert the changeset (push)")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no changeset with the specified id is available in the repository, the branch or the repository itself does not exist",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "409",
    description = "conflict, the revert could not be performed automatically because of conflicts in the changes",
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
  public Response revert(@PathParam("namespace") String namespace,
                         @PathParam("name") String name,
                         @PathParam("id") String id,
                         RevertDto revertDto) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      RepositoryPermissions.push(repositoryService.getRepository()).check();
      RevertCommandBuilder command = repositoryService.getRevertCommand()
        .setRevision(id);
        command.setBranch(revertDto.branch());
        command.setMessage(revertDto.message());

       RevertCommandResult result = command.execute();

      if (result.isSuccessful()) {
        return Response.
          created(URI.create(resourceLinks.changeset().changeset(namespace, name, result.getRevision())))
          .entity(new RevertResponseDto(result.getRevision()))
          .build();
      } else {
        throw new ConflictException(
          new NamespaceAndName(namespace, name),
          result.getFilesWithConflict()
        );
      }
    }
  }

  public record RevertDto(String branch, String message) {
  }

  public record RevertResponseDto(String revision) {
  }
}
