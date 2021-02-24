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

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import sonia.scm.BadRequestException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NotFoundException;
import sonia.scm.Type;
import sonia.scm.importexport.ExportService;
import sonia.scm.importexport.FullScmRepositoryExporter;
import sonia.scm.importexport.RepositoryImportExportEncryption;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.BundleCommandBuilder;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.web.VndMediaType;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.api.v2.resources.RepositoryTypeSupportChecker.checkSupport;
import static sonia.scm.api.v2.resources.RepositoryTypeSupportChecker.type;

public class RepositoryExportResource {

  private static final String NO_PASSWORD = "";

  private final RepositoryManager manager;
  private final RepositoryServiceFactory serviceFactory;
  private final FullScmRepositoryExporter fullScmRepositoryExporter;
  private final RepositoryImportExportEncryption repositoryImportExportEncryption;
  private final ExecutorService repositoryExportHandler;
  private final ExportService exportService;
  private final RepositoryExportInformationToDtoMapper informationToDtoMapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryExportResource(RepositoryManager manager,
                                  RepositoryServiceFactory serviceFactory,
                                  FullScmRepositoryExporter fullScmRepositoryExporter,
                                  RepositoryImportExportEncryption repositoryImportExportEncryption,
                                  ExportService exportService,
                                  RepositoryExportInformationToDtoMapper informationToDtoMapper,
                                  ResourceLinks resourceLinks) {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
    this.fullScmRepositoryExporter = fullScmRepositoryExporter;
    this.repositoryImportExportEncryption = repositoryImportExportEncryption;
    this.exportService = exportService;
    this.informationToDtoMapper = informationToDtoMapper;
    this.resourceLinks = resourceLinks;
    this.repositoryExportHandler = this.createExportHandlerPool();
  }

  /**
   * Exports an existing repository without additional metadata. The method can
   * only be used, if the repository type supports the {@link Command#BUNDLE}.
   *
   * @param uriInfo   uri info
   * @param namespace namespace of the repository
   * @param name      name of the repository
   * @param type      type of the repository
   * @return response with readable stream of repository dump
   * @since 2.13.0
   */
  @GET
  @Path("{type: ^(?!full$)[^/]+$}")
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
                                   @Pattern(regexp = "\\w{1,10}") @PathParam("type") String type,
                                   @DefaultValue("false") @QueryParam("compressed") boolean compressed
  ) {
    Repository repository = getVerifiedRepository(namespace, name, type);
    RepositoryPermissions.export(repository).check();
    return exportRepository(repository, NO_PASSWORD, compressed, false);
  }

  /**
   * Exports an existing repository with all additional metadata and environment information. The method can
   * only be used, if the repository type supports the {@link Command#BUNDLE}.
   *
   * @param uriInfo   uri info
   * @param namespace namespace of the repository
   * @param name      name of the repository
   * @return response with readable stream of repository dump
   * @since 2.13.0
   */
  @GET
  @Path("full")
  @Operation(summary = "Exports the repository", description = "Exports the repository with metadata and environment information.", tags = "Repository")
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
  public Response exportFullRepository(@Context UriInfo uriInfo,
                                       @PathParam("namespace") String namespace,
                                       @PathParam("name") String name
  ) {
    Repository repository = getVerifiedRepository(namespace, name);
    RepositoryPermissions.export(repository).check();
    return exportFullRepository(repository, NO_PASSWORD, false);
  }

  /**
   * Exports an existing repository without additional metadata. The method can
   * only be used, if the repository type supports the {@link Command#BUNDLE}.
   *
   * @param uriInfo   uri info
   * @param namespace namespace of the repository
   * @param name      name of the repository
   * @param type      type of the repository
   * @param request   request of repository export which contains the password
   * @return response with readable stream of repository dump
   * @since 2.14.0
   */
  @POST
  @Path("{type: ^(?!full$)[^/]+$}")
  @Consumes(VndMediaType.REPOSITORY_EXPORT)
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
    responseCode = "409",
    description = "Repository export already started."
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response exportRepositoryWithPassword(@Context UriInfo uriInfo,
                                               @PathParam("namespace") String namespace,
                                               @PathParam("name") String name,
                                               @Pattern(regexp = "\\w{1,10}") @PathParam("type") String type,
                                               @DefaultValue("false") @QueryParam("compressed") boolean compressed,
                                               @Valid ExportDto request
  ) throws Exception {
    Repository repository = getVerifiedRepository(namespace, name, type);
    RepositoryPermissions.export(repository).check();
    checkRepositoryIsAlreadyExporting(repository);
    return exportAsync(repository, request, () -> {
      Response response = exportRepository(repository, request.getPassword(), compressed, request.isAsync());
      exportService.setExportFinished(repository);
      return response;
    });
  }

  /**
   * Exports an existing repository with all additional metadata and environment information. The method can
   * only be used, if the repository type supports the {@link Command#BUNDLE}.
   *
   * @param uriInfo   uri info
   * @param namespace namespace of the repository
   * @param name      name of the repository
   * @param request   request of repository export which contains the password
   * @return response with readable stream of repository dump
   * @since 2.14.0
   */
  @POST
  @Path("full")
  @Consumes(VndMediaType.REPOSITORY_EXPORT)
  @Operation(summary = "Exports the repository", description = "Exports the repository with metadata and environment information.", tags = "Repository")
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
  public Response exportFullRepositoryWithPassword(@Context UriInfo uriInfo,
                                                   @PathParam("namespace") String namespace,
                                                   @PathParam("name") String name,
                                                   @Valid ExportDto request
  ) throws Exception {
    Repository repository = getVerifiedRepository(namespace, name);
    RepositoryPermissions.export(repository).check();
    checkRepositoryIsAlreadyExporting(repository);
    return exportAsync(repository, request, () -> {
      Response response = exportFullRepository(repository, request.getPassword(), request.isAsync());
      exportService.setExportFinished(repository);
      return response;
    });
  }

  @DELETE
  @Path("")
  @Operation(summary = "Deletes repository export", description = "Deletes repository export if stored.", tags = "Repository")
  @ApiResponse(
    responseCode = "204",
    description = "Repository export was deleted"
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response deleteExport(@Context UriInfo uriInfo,
                               @PathParam("namespace") String namespace,
                               @PathParam("name") String name) {
    Repository repository = getVerifiedRepository(namespace, name);
    RepositoryPermissions.export(repository).check();
    exportService.clear(repository.getId());
    return Response.noContent().build();
  }

  @GET
  @Path("download")
  @Operation(summary = "Download stored repository export", description = "Download the stored repository export.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "Repository export was downloaded"
  )
  @ApiResponse(
    responseCode = "404",
    description = "Repository export not found"
  )
  @ApiResponse(
    responseCode = "409",
    description = "Repository export is not ready yet"
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response downloadExport(@Context UriInfo uriInfo,
                                 @PathParam("namespace") String namespace,
                                 @PathParam("name") String name) {
    Repository repository = getVerifiedRepository(namespace, name);
    RepositoryPermissions.export(repository).check();
    checkRepositoryIsAlreadyExporting(repository);
    exportService.checkExportIsAvailable(repository);
    StreamingOutput output = os -> {
      try (InputStream is = exportService.getData(repository)) {
        IOUtil.copy(is, os);
      }
    };
    String fileExtension = exportService.getFileExtension(repository);
    return createResponse(repository, fileExtension, fileExtension.contains(".gz"), output);
  }

  @GET
  @Produces(VndMediaType.REPOSITORY_EXPORT_INFO)
  @Path("info")
  @Operation(summary = "Returns stored repository export information", description = "Returns the stored repository export information.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "Repository export information"
  )
  @ApiResponse(
    responseCode = "404",
    description = "Repository export information not found"
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public RepositoryExportInformationDto getExportInformation(@Context UriInfo uriInfo,
                                                             @PathParam("namespace") String namespace,
                                                             @PathParam("name") String name) {
    Repository repository = getVerifiedRepository(namespace, name);
    RepositoryPermissions.export(repository).check();
    return informationToDtoMapper.map(exportService.getExportInformation(repository), repository);
  }

  private Response exportAsync(Repository repository, ExportDto exportDto, Callable<Response> call) throws
    Exception {
    if (exportDto.isAsync()) {
      repositoryExportHandler.submit(call);
      return Response.status(202).header(
        "SCM-Export-Download",
        resourceLinks.repository().downloadExport(repository.getNamespace(), repository.getName())
      ).build();
    } else {
      return call.call();
    }
  }

  private void checkRepositoryIsAlreadyExporting(Repository repository) {
    if (exportService.isExporting(repository)) {
      //TODO Throw specific exception that repository is still exporting which is resolved to 409
      throw new ConcurrentModificationException(Repository.class, repository.getId());
    }
  }

  private Repository getVerifiedRepository(String namespace, String name) {
    Repository repository = manager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw new NotFoundException(Repository.class, namespace + "/" + name);
    }
    RepositoryPermissions.read().check(repository);
    return repository;
  }

  private Repository getVerifiedRepository(String namespace, String name, String type) {
    Repository repository = getVerifiedRepository(namespace, name);

    if (!type.equals(repository.getType())) {
      throw new WrongTypeException(repository);
    }
    Type repositoryType = type(manager, type);
    checkSupport(repositoryType, Command.BUNDLE);
    return repository;
  }

  private Response exportFullRepository(Repository repository, String password, boolean async) {
    String fileExtension = resolveFullExportFileExtension(password);
    if (async) {
      OutputStream blobOutputStream = exportService.store(repository, true, true, !Strings.isNullOrEmpty(password));
      fullScmRepositoryExporter.export(repository, blobOutputStream, password);
      exportService.setExportFinished(repository);
      //TODO Cleanup
      // Return value will be ignored for async export
      return null;
    } else {
      StreamingOutput output = os -> fullScmRepositoryExporter.export(repository, os, password);

      return Response
        .ok(output, "application/x-gzip")
        .header("content-disposition", createContentDispositionHeaderValue(repository, fileExtension))
        .build();
    }
  }

  private String resolveFullExportFileExtension(String password) {
    if (!Strings.isNullOrEmpty(password)) {
      return "tar.gz.enc";
    }
    return "tar.gz";
  }

  private Response exportRepository(Repository repository, String password, boolean compressed, boolean async) {
    StreamingOutput output;
    String fileExtension;
    try (final RepositoryService service = serviceFactory.create(repository)) {
      BundleCommandBuilder bundleCommand = service.getBundleCommand();
      fileExtension = resolveFileExtension(bundleCommand, compressed, !Strings.isNullOrEmpty(password));
      output = os -> {
        try {
          OutputStream cos;
          if (async) {
            OutputStream blobOutputStream = exportService.store(repository, false, compressed, !Strings.isNullOrEmpty(password));
            cos = repositoryImportExportEncryption.optionallyEncrypt(blobOutputStream, password);
          } else {
            cos = repositoryImportExportEncryption.optionallyEncrypt(os, password);
          }
          if (compressed) {
            GzipCompressorOutputStream gzipCompressorOutputStream = new GzipCompressorOutputStream(cos);
            bundleCommand.bundle(gzipCompressorOutputStream);
            gzipCompressorOutputStream.finish();
          } else {
            bundleCommand.bundle(os);
          }
        } catch (IOException e) {
          throw new ExportFailedException(entity(repository).build(), "repository export failed", e);
        }
      };
    }

    return createResponse(repository, fileExtension, compressed, output);
  }

  private Response createResponse(Repository repository, String fileExtension, boolean compressed, StreamingOutput
    output) {
    return Response
      .ok(output, compressed ? "application/x-gzip" : MediaType.APPLICATION_OCTET_STREAM)
      .header("content-disposition", createContentDispositionHeaderValue(repository, fileExtension))
      .build();
  }

  private String resolveFileExtension(BundleCommandBuilder bundleCommand, boolean compressed, boolean encrypted) {
    StringBuilder fileExtensionBuilder = new StringBuilder(bundleCommand.getFileExtension());
    if (compressed) {
      fileExtensionBuilder.append(".gz");
    }
    if (encrypted) {
      fileExtensionBuilder.append(".enc");
    }
    return fileExtensionBuilder.toString();
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

  private ExecutorService createExportHandlerPool() {
    return Executors.newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setNameFormat("RepositoryExportHandler-%d")
        .build()
    );
  }


  private static class WrongTypeException extends BadRequestException {

    private static final String CODE = "4hSNNTBiu1";

    public WrongTypeException(Repository repository) {
      super(entity(repository).build(), "illegal type for repository");
    }

    @Override
    public String getCode() {
      return CODE;
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  private static class ExportDto {
    private String password;
    private boolean async;
  }
}
