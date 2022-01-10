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
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

public class CompareDiffResource {

  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  static final String DIFF_FORMAT_VALUES_REGEX = "NATIVE|GIT|UNIFIED";

  private final RepositoryServiceFactory serviceFactory;
  private final DiffResultToDiffResultDtoMapper parsedDiffMapper;

  @Inject
  public CompareDiffResource(RepositoryServiceFactory serviceFactory, DiffResultToDiffResultDtoMapper parsedDiffMapper) {
    this.serviceFactory = serviceFactory;
    this.parsedDiffMapper = parsedDiffMapper;
  }


  /**
   * Get the repository diff of a revision
   *
   * @param namespace repository namespace
   * @param name      repository name
   * @param source    the first revision for compare
   * @param target    the second revision for compare
   * @return the diff of the revision
   * @throws NotFoundException if the repository is not found
   */
  @GET
  @Path("{source}/{target}")
  @Produces(VndMediaType.DIFF)
  @Operation(summary = "Compare two revisions", description = "Get the repository diff between two compared revision.", tags = "Repository")
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
  public Response get(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name,
    @PathParam("source") String source,
    @PathParam("target") String target,
    @Pattern(regexp = DIFF_FORMAT_VALUES_REGEX) @DefaultValue("NATIVE") @QueryParam("format") String format
  ) throws IOException {
    HttpUtil.checkForCRLFInjection(source);
    HttpUtil.checkForCRLFInjection(target);
    DiffFormat diffFormat = DiffFormat.valueOf(format);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      DiffCommandBuilder.OutputStreamConsumer outputStreamConsumer = repositoryService.getDiffCommand()
        .setRevision(source)
        .setCompareTo(target)
        .setFormat(diffFormat)
        .retrieveContent();
      return Response.ok((StreamingOutput) outputStreamConsumer::accept)
        .header(HEADER_CONTENT_DISPOSITION, HttpUtil.createContentDispositionAttachmentHeader(String.format("%s-%s.diff", name, source)))
        .build();
    }
  }

  @GET
  @Path("{source}/{target}/parsed")
  @Produces(VndMediaType.DIFF_PARSED)
  @Operation(summary = "Parsed diff by compared revisions", description = "Get the parsed repository diff of two compared revisions.", tags = "Repository")
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
                                 @PathParam("source") String source,
                                 @PathParam("target") String target,
                                 @QueryParam("limit") @Min(1) Integer limit,
                                 @QueryParam("offset") @Min(0) Integer offset) throws IOException {
    HttpUtil.checkForCRLFInjection(source);
    HttpUtil.checkForCRLFInjection(target);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      DiffResult diffResult = repositoryService.getDiffResultCommand()
        .setRevision(source)
        .setCompareTo(target)
        .setLimit(limit)
        .setOffset(offset)
        .getDiffResult();
      return parsedDiffMapper.mapForRevision(repositoryService.getRepository(), diffResult, source);
    }
  }
}
