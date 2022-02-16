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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.io.ContentType;
import sonia.scm.io.ContentTypeResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;

public class ContentResource {

  private static final Logger LOG = LoggerFactory.getLogger(ContentResource.class);
  private static final int HEAD_BUFFER_SIZE = 1024;

  private final RepositoryServiceFactory serviceFactory;
  private final ContentTypeResolver contentTypeResolver;

  @Inject
  public ContentResource(RepositoryServiceFactory serviceFactory, ContentTypeResolver contentTypeResolver) {
    this.serviceFactory = serviceFactory;
    this.contentTypeResolver = contentTypeResolver;
  }

  /**
   * Returns the content of a file for the given revision in the repository. The content type depends on the file
   * content and can be discovered calling <code>HEAD</code> on the same URL. If a programming languge could be
   * recognized, this will be given in the header <code>Language</code>.
   *  @param namespace the namespace of the repository
   * @param name      the name of the repository
   * @param revision  the revision
   * @param path      The path of the file
   * @param start
   * @param end
   */
  @GET
  @Path("{revision}/{path: .*}")
  @Operation(summary = "File content by revision", description = "Returns the content of a file for the given revision in the repository.", tags = "Repository")
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
  @Parameter(
    name = "start",
    description = "If set, the content will be returned from this line on. The first line is line number 0. " +
      "If omitted, the output will start with the first line."
  )
  @Parameter(
    name = "end",
    description = "If set, the content will be returned excluding the given line number and following." +
      "The first line ist line number 0. " +
      "If set to -1, no lines will be excluded (this equivalent to omitting this parameter"
  )
  public Response get(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name,
    @PathParam("revision") String revision,
    @PathParam("path") String path,
    @QueryParam("start") Integer start,
    @QueryParam("end") Integer end) {
    StreamingOutput stream = createStreamingOutput(namespace, name, revision, path, start, end);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Response.ResponseBuilder responseBuilder = Response.ok(stream);
      return createContentHeader(namespace, name, revision, path, repositoryService, responseBuilder);
    } catch (NotFoundException e) {
      LOG.debug(e.getMessage());
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private StreamingOutput createStreamingOutput(String namespace, String name, String revision, String path, Integer start, Integer end) {
    Integer effectiveEnd;
    if (end != null && end < 0) {
      effectiveEnd = null;
    } else {
      effectiveEnd = end;
    }
    return os -> {
      OutputStream sourceOut;
      if (start != null || effectiveEnd != null) {
        sourceOut = new LineFilteredOutputStream(os, start, effectiveEnd);
      } else {
        sourceOut = os;
      }
      try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
        repositoryService.getCatCommand().setRevision(revision).retriveContent(sourceOut, path);
        sourceOut.close();
      } catch (NotFoundException e) {
        LOG.debug(e.getMessage());
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    };
  }

  /**
   * Returns the content type and the programming language (if it can be detected) of a file for the given revision in
   * the repository. The programming language will be given in the header <code>Language</code>.
   *
   * @param namespace the namespace of the repository
   * @param name      the name of the repository
   * @param revision  the revision
   * @param path      The path of the file
   */
  @HEAD
  @Path("{revision}/{path: .*}")
  @Operation(
    summary = "File metadata by revision",
    description = "Returns the content type and the programming language (if it can be detected) of a file for the given revision in the repository.",
    tags = "Repository"
  )
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
  public Response metadata(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Response.ResponseBuilder responseBuilder = Response.ok();
      return createContentHeader(namespace, name, revision, path, repositoryService, responseBuilder);
    } catch (NotFoundException e) {
      LOG.debug(e.getMessage());
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private Response createContentHeader(String namespace, String name, String revision, String path, RepositoryService repositoryService, Response.ResponseBuilder responseBuilder) {
    try {
      appendContentHeader(path, getHead(revision, path, repositoryService), responseBuilder);
    } catch (IOException e) {
      LOG.info("error reading repository resource {} from {}/{}", path, namespace, name, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
    return responseBuilder.build();
  }

  private void appendContentHeader(String path, byte[] head, Response.ResponseBuilder responseBuilder) {
    ContentType contentType = contentTypeResolver.resolve(path, head);
    responseBuilder.header("Content-Type", contentType.getRaw());
    contentType.getLanguage().ifPresent(
      language -> responseBuilder.header(ProgrammingLanguages.HEADER, language)
    );

    contentType.getSyntaxModes().forEach((mode, lang) -> {
      String modeName = mode.substring(0, 1).toUpperCase(Locale.ENGLISH) + mode.substring(1);
      responseBuilder.header(ProgrammingLanguages.HEADER_SYNTAX_MODE_PREFIX + modeName, lang);
    });
  }

  private byte[] getHead(String revision, String path, RepositoryService repositoryService) throws IOException {
    InputStream stream = repositoryService.getCatCommand().setRevision(revision).getStream(path);
    try {
      byte[] buffer = new byte[HEAD_BUFFER_SIZE];
      int length = stream.read(buffer);
      if (length < 0) { // empty file
        return new byte[]{};
      } else if (length < buffer.length) {
        return Arrays.copyOf(buffer, length);
      } else {
        return buffer;
      }
    } finally {
      IOUtil.close(stream);
    }
  }
}
