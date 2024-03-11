/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;


@Slf4j
public class ChangesetRootResource {

  private final RepositoryServiceFactory serviceFactory;

  private final ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper;

  private final ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper;

  @Inject
  public ChangesetRootResource(RepositoryServiceFactory serviceFactory, ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper, ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.changesetCollectionToDtoMapper = changesetCollectionToDtoMapper;
    this.changesetToChangesetDtoMapper = changesetToChangesetDtoMapper;
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
}
