package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.repository.HgConfig;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class HgConfigInstallationsResource {

  public static final String PATH_HG = "hg";
  public static final String PATH_PYTHON = "python";
  private final HgConfigInstallationsToDtoMapper hgConfigInstallationsToDtoMapper;

  @Inject
  public HgConfigInstallationsResource(HgConfigInstallationsToDtoMapper hgConfigInstallationsToDtoMapper) {
    this.hgConfigInstallationsToDtoMapper = hgConfigInstallationsToDtoMapper;
  }

  /**
   * Returns the hg installations.
   */
  @GET
  @Path(PATH_HG)
  @Produces(HgVndMediaType.INSTALLATIONS)
  @Operation(summary = "Hg installations", description = "Returns the mercurial installations.", tags = "Mercurial")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = HgVndMediaType.INSTALLATIONS,
      schema = @Schema(implementation = HgConfigInstallationsDto.class)
    )
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
  public HalRepresentation getHgInstallations() {

    ConfigurationPermissions.read(HgConfig.PERMISSION).check();

    return hgConfigInstallationsToDtoMapper.map(
      HgInstallerFactory.createInstaller().getHgInstallations(), PATH_HG);
  }

  /**
   * Returns the python installations.
   */
  @GET
  @Path(PATH_PYTHON)
  @Produces(HgVndMediaType.INSTALLATIONS)
  @Operation(summary = "Python installations", description = "Returns the python installations.", tags = "Mercurial")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = HgVndMediaType.INSTALLATIONS,
      schema = @Schema(implementation = HgConfigInstallationsDto.class)
    )
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
  public HalRepresentation getPythonInstallations() {

    ConfigurationPermissions.read(HgConfig.PERMISSION).check();

    return hgConfigInstallationsToDtoMapper.map(
      HgInstallerFactory.createInstaller().getPythonInstallations(), PATH_PYTHON);
  }
}
