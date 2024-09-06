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

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static sonia.scm.api.v2.resources.DiffRootResource.DIFF_FORMAT_VALUES_REGEX;
import static sonia.scm.api.v2.resources.DiffRootResource.HEADER_CONTENT_DISPOSITION;

public class IncomingRootResource {

  private final RepositoryServiceFactory serviceFactory;

  private final IncomingChangesetCollectionToDtoMapper changesetMapper;
  private final DiffResultToDiffResultDtoMapper parsedDiffMapper;

  @Inject
  public IncomingRootResource(RepositoryServiceFactory serviceFactory, IncomingChangesetCollectionToDtoMapper incomingChangesetCollectionToDtoMapper, DiffResultToDiffResultDtoMapper parsedDiffMapper) {
    this.serviceFactory = serviceFactory;
    this.changesetMapper = incomingChangesetCollectionToDtoMapper;
    this.parsedDiffMapper = parsedDiffMapper;
  }

  /**
   * Get the incoming changesets from <code>source</code> to <code>target</code>
   * <p>
   * Example:
   * <p>
   * -                          master
   * -                            |
   * -            _______________ ° m1
   * -           e                |
   * -           |                ° m2
   * -           ° e1             |
   * -    ______|_______          |
   * -   |             |          b
   * -   f             a          |
   * -   |             |          ° b1
   * -   ° f1          ° a1       |
   * -                            ° b2
   * -
   * <p>
   * - /incoming/a/master/changesets  -&gt; a1 , e1
   * - /incoming/b/master/changesets  -&gt; b1 , b2
   * - /incoming/b/f/changesets       -&gt; b1 , b2, m2
   * - /incoming/f/b/changesets       -&gt; f1 , e1
   * - /incoming/a/b/changesets       -&gt; a1 , e1
   * - /incoming/a/b/changesets       -&gt; a1 , e1
   *
   * @param namespace
   * @param name
   * @param source    can be a changeset id or a branch name
   * @param target    can be a changeset id or a branch name
   * @param page
   * @param pageSize
   * @return
   * @throws Exception
   */
  @GET
  @Path("{source}/{target}/changesets")
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @Operation(summary = "Incoming changesets", description = "Get the incoming changesets from source to target.", tags = "Repository")
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
  public Response incomingChangesets(@PathParam("namespace") String namespace,
                                     @PathParam("name") String name,
                                     @PathParam("source") String source,
                                     @PathParam("target") String target,
                                     @DefaultValue("0") @QueryParam("page") int page,
                                     @DefaultValue("10") @QueryParam("pageSize") int pageSize) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      ChangesetPagingResult changesets = new PagedLogCommandBuilder(repositoryService)
        .page(page)
        .pageSize(pageSize)
        .create()
        .setStartChangeset(source)
        .setAncestorChangeset(target)
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        return Response.ok(changesetMapper.map(page, pageSize, pageResult, repository, source, target)).build();
      } else {
        return Response.ok().build();
      }
    }
  }

  @GET
  @Path("{source}/{target}/diff")
  @Produces(VndMediaType.DIFF)
  @Operation(summary = "Incoming diff", description = "Get the incoming diff from source to target.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.DIFF,
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
  public Response incomingDiff(@PathParam("namespace") String namespace,
                               @PathParam("name") String name,
                               @PathParam("source") String source,
                               @PathParam("target") String target,
                               @QueryParam("ignoreWhitespace") @DefaultValue("NONE") IgnoreWhitespaceLevel ignoreWhitespace,
                               @Pattern(regexp = DIFF_FORMAT_VALUES_REGEX) @DefaultValue("NATIVE") @QueryParam("format") String format) throws IOException {

    HttpUtil.checkForCRLFInjection(source);
    HttpUtil.checkForCRLFInjection(target);
    DiffFormat diffFormat = DiffFormat.valueOf(format);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      DiffCommandBuilder.OutputStreamConsumer outputStreamConsumer = repositoryService.getDiffCommand()
        .setRevision(source)
        .setAncestorChangeset(target)
        .setFormat(diffFormat)
        .setIgnoreWhitespace(ignoreWhitespace)
        .retrieveContent();

      return Response.ok((StreamingOutput) outputStreamConsumer::accept)
        .header(HEADER_CONTENT_DISPOSITION, HttpUtil.createContentDispositionAttachmentHeader(String.format("%s-%s.diff", name, source)))
        .build();
    }
  }

  @GET
  @Path("{source}/{target}/diff/parsed")
  @Produces(VndMediaType.DIFF_PARSED)
  @Operation(summary = "Incoming parsed diff", description = "Get the incoming parsed diff from source to target.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.DIFF_PARSED,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "bad request")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the diff")
  @ApiResponse(
    responseCode = "404",
    description = "not found, source or target branch for the repository not available or repository not found",
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
  public Response incomingDiffParsed(@PathParam("namespace") String namespace,
                                     @PathParam("name") String name,
                                     @PathParam("source") String source,
                                     @PathParam("target") String target,
                                     @QueryParam("limit") @Min(1) Integer limit,
                                     @QueryParam("ignoreWhitespace") @DefaultValue("NONE") IgnoreWhitespaceLevel ignoreWhitespace,
                                     @QueryParam("offset") @Min(0) Integer offset) throws IOException {
    HttpUtil.checkForCRLFInjection(source);
    HttpUtil.checkForCRLFInjection(target);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      DiffResult diffResult = repositoryService.getDiffResultCommand()
        .setRevision(source)
        .setAncestorChangeset(target)
        .setLimit(limit)
        .setIgnoreWhitespace(ignoreWhitespace)
        .setOffset(offset)
        .getDiffResult();
      return Response.ok(parsedDiffMapper.mapForIncoming(repositoryService.getRepository(), diffResult, source, target)).build();
    }
  }
}
