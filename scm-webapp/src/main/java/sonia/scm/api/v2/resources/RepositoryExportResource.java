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

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import sonia.scm.Type;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.BundleCommandBuilder;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.time.Instant;

import static sonia.scm.api.v2.resources.RepositoryTypeSupportChecker.checkSupport;
import static sonia.scm.api.v2.resources.RepositoryTypeSupportChecker.type;

public class RepositoryExportResource {

  private final RepositoryManager manager;
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryExportResource(RepositoryManager manager,
                                  RepositoryServiceFactory serviceFactory) {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
  }

  /**
   * Exports an existing repository without additional metadata. The method can
   * only be used, if the repository type supports the {@link Command#BUNDLE}.
   *
   * @param uriInfo   uri info
   * @param namespace of the repository
   * @param name      of the repository
   * @param type      of the repository
   * @return response with readable stream of repository dump
   * @since 2.13.0
   */
  @GET
  @Path("{type}")
  @Consumes(VndMediaType.REPOSITORY)
  @Operation(summary = "Exports the repository", description = "Exports the repository.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "Repository export was successful"
  )
  @ApiResponse(
    responseCode = "401",
    description = "not authenticated / invalid credentials"
  )
  @ApiResponse(
    responseCode = "403",
    description = "not authorized, the current user has no privileges to read the repository"
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response exportRepository(@Context UriInfo uriInfo,
                                   @PathParam("namespace") String namespace,
                                   @PathParam("name") String name,
                                   @PathParam("type") String type,
                                   @DefaultValue("false") @QueryParam("compressed") boolean compressed
  ) {
    Repository repository = manager.get(new NamespaceAndName(namespace, name));
    RepositoryPermissions.read().check(repository);

    Type repositoryType = type(manager, type);
    checkSupport(repositoryType, Command.BUNDLE);

    return exportRepository(repository, compressed);
  }

  private Response exportRepository(Repository repository, boolean compressed) {
    StreamingOutput output;
    String fileExtension;
    try (final RepositoryService service = serviceFactory.create(repository)) {
      BundleCommandBuilder bundleCommand = service.getBundleCommand();
      fileExtension = resolveFileExtension(bundleCommand, compressed);
      output = os -> {
        try {
          if (compressed) {
            GzipCompressorOutputStream gzipCompressorOutputStream = new GzipCompressorOutputStream(os);
            bundleCommand.bundle(gzipCompressorOutputStream);
            gzipCompressorOutputStream.finish();
          } else {
            bundleCommand.bundle(os);
          }
        } catch (IOException e) {
          throw new InternalRepositoryException(repository, "repository export failed", e);
        }
      };
    }

    return Response
      .ok(output, compressed ? "application/x-gzip" : MediaType.APPLICATION_OCTET_STREAM)
      .header("content-disposition", createContentDispositionHeaderValue(repository, fileExtension))
      .build();
  }

  private String resolveFileExtension(BundleCommandBuilder bundleCommand, boolean compressed) {
    if (compressed) {
      return bundleCommand.getFileExtension() + ".gz";
    } else {
      return bundleCommand.getFileExtension();
    }
  }

  private String createContentDispositionHeaderValue(Repository repository, String fileExtension) {
    String timestamp = createFormattedTimestamp();
    return String.format(
      "attachment; filename = %s-%s-%s.%s",
      repository.getNamespace(),
      repository.getName(),
      timestamp,
      fileExtension
    );
  }

  private String createFormattedTimestamp() {
    return Instant.now().toString().replace(":", "-").split("\\.")[0];
  }
}
