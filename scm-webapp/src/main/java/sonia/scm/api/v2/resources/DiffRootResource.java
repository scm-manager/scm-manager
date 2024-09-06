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
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

public class DiffRootResource {

  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  static final String DIFF_FORMAT_VALUES_REGEX = "NATIVE|GIT|UNIFIED";

  private final RepositoryServiceFactory serviceFactory;
  private final DiffResultToDiffResultDtoMapper parsedDiffMapper;

  @Inject
  public DiffRootResource(RepositoryServiceFactory serviceFactory, DiffResultToDiffResultDtoMapper parsedDiffMapper) {
    this.serviceFactory = serviceFactory;
    this.parsedDiffMapper = parsedDiffMapper;
  }


  /**
   * Get the repository diff of a revision
   *
   * @param namespace repository namespace
   * @param name      repository name
   * @param revision  the revision
   * @return the dif of the revision
   * @throws NotFoundException if the repository is not found
   */
  @GET
  @Path("{revision}")
  @Produces(VndMediaType.DIFF)
  @Operation(summary = "Diff by revision", description = "Get the repository diff of a revision.", tags = "Repository")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "400", description = "bad request")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the diff")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no revision with the specified param for the repository available or repository found",
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
  public Response get(@PathParam("namespace") String namespace,
                      @PathParam("name") String name,
                      @PathParam("revision") String revision,
                      @QueryParam("format") @Pattern(regexp = DIFF_FORMAT_VALUES_REGEX) @DefaultValue("NATIVE") String format,
                      @QueryParam("ignoreWhitespace") @DefaultValue("NONE") IgnoreWhitespaceLevel ignoreWhitespace) throws IOException {
    HttpUtil.checkForCRLFInjection(revision);
    DiffFormat diffFormat = DiffFormat.valueOf(format);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      DiffCommandBuilder.OutputStreamConsumer outputStreamConsumer = repositoryService.getDiffCommand()
        .setRevision(revision)
        .setFormat(diffFormat)
        .setIgnoreWhitespace(ignoreWhitespace)
        .retrieveContent();
      return Response.ok((StreamingOutput) outputStreamConsumer::accept)
        .header(HEADER_CONTENT_DISPOSITION, HttpUtil.createContentDispositionAttachmentHeader(String.format("%s-%s.diff", name, revision)))
        .build();
    }
  }

  @GET
  @Path("{revision}/parsed")
  @Produces(VndMediaType.DIFF_PARSED)
  @Operation(summary = "Parsed diff by revision", description = "Get the parsed repository diff of a revision.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.DIFF_PARSED,
      schema = @Schema(implementation = DiffResultDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "bad request")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the diff")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no revision with the specified param for the repository available or repository not found",
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
  public DiffResultDto getParsed(@PathParam("namespace") String namespace,
                                 @PathParam("name") String name,
                                 @PathParam("revision") String revision,
                                 @QueryParam("limit") @Min(1) Integer limit,
                                 @QueryParam("ignoreWhitespace") @DefaultValue("NONE") IgnoreWhitespaceLevel ignoreWhitespace,
                                 @QueryParam("offset") @Min(0) Integer offset) throws IOException {
    HttpUtil.checkForCRLFInjection(revision);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      DiffResult diffResult = repositoryService.getDiffResultCommand()
        .setRevision(revision)
        .setLimit(limit)
        .setOffset(offset)
        .setIgnoreWhitespace(ignoreWhitespace)
        .getDiffResult();
      return parsedDiffMapper.mapForRevision(repositoryService.getRepository(), diffResult, revision);
    }
  }
}
