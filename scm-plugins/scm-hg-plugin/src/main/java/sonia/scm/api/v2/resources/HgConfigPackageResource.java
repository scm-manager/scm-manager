package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.SCMContext;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.installer.HgPackage;
import sonia.scm.installer.HgPackageReader;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class HgConfigPackageResource {

  private final HgPackageReader pkgReader;
  private final AdvancedHttpClient client;
  private final HgRepositoryHandler handler;
  private final HgConfigPackagesToDtoMapper configPackageCollectionToDtoMapper;

  @Inject
  public HgConfigPackageResource(HgPackageReader pkgReader, AdvancedHttpClient client, HgRepositoryHandler handler,
                                 HgConfigPackagesToDtoMapper hgConfigPackagesToDtoMapper) {
    this.pkgReader = pkgReader;
    this.client = client;
    this.handler = handler;
    this.configPackageCollectionToDtoMapper = hgConfigPackagesToDtoMapper;
  }

  /**
   * Returns all mercurial packages.
   */
  @GET
  @Path("")
  @Produces(HgVndMediaType.PACKAGES)
  @Operation(summary = "Hg configuration packages", description = "Returns all mercurial packages.", tags = "Mercurial")
  @ApiResponse(
    responseCode = "204",
    description = "update success"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:hg\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public HalRepresentation getPackages() {

    ConfigurationPermissions.read(HgConfig.PERMISSION).check();

    return configPackageCollectionToDtoMapper.map(pkgReader.getPackages());
  }

  /**
   * Installs a mercurial package
   *
   * @param pkgId Identifier of the package to install
   */
  @PUT
  @Path("{pkgId}")
  @Operation(summary = "Modifies hg configuration package", description = "Installs a mercurial package.", tags = "Mercurial")
  @ApiResponse(
    responseCode = "204",
    description = "update success"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:hg\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "no package found for id",
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
  public Response installPackage(@PathParam("pkgId") String pkgId) {
    Response response;

    ConfigurationPermissions.write(HgConfig.PERMISSION).check();

    HgPackage pkg = pkgReader.getPackage(pkgId);

    if (pkg != null) {
      if (HgInstallerFactory.createInstaller()
        .installPackage(client, handler, SCMContext.getContext().getBaseDirectory(), pkg)) {
        response = Response.noContent().build();
      } else {
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } else {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }

    return response;
  }
}
