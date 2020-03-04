package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * RESTful Web Service Resource to manage the configuration of the hg plugin.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Mercurial", description = "Configuration for the mercurial repository type")
})
@Path(HgConfigResource.HG_CONFIG_PATH_V2)
public class HgConfigResource {

  static final String HG_CONFIG_PATH_V2 = "v2/config/hg";
  private final HgConfigDtoToHgConfigMapper dtoToConfigMapper;
  private final HgConfigToHgConfigDtoMapper configToDtoMapper;
  private final HgRepositoryHandler repositoryHandler;
  private final Provider<HgConfigPackageResource> packagesResource;
  private final Provider<HgConfigAutoConfigurationResource> autoconfigResource;
  private final Provider<HgConfigInstallationsResource> installationsResource;

  @Inject
  public HgConfigResource(HgConfigDtoToHgConfigMapper dtoToConfigMapper, HgConfigToHgConfigDtoMapper configToDtoMapper,
                          HgRepositoryHandler repositoryHandler, Provider<HgConfigPackageResource> packagesResource,
                          Provider<HgConfigAutoConfigurationResource> autoconfigResource,
                          Provider<HgConfigInstallationsResource> installationsResource) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.repositoryHandler = repositoryHandler;
    this.packagesResource = packagesResource;
    this.autoconfigResource = autoconfigResource;
    this.installationsResource = installationsResource;
  }

  /**
   * Returns the hg config.
   */
  @GET
  @Path("")
  @Produces(HgVndMediaType.CONFIG)
  @Operation(summary = "Hg configuration", description = "Returns the global mercurial configuration.", tags = "Mercurial", operationId = "hg_get_config")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = HgVndMediaType.CONFIG,
      schema = @Schema(implementation = HgConfigDto.class)
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
  public Response get() {

    ConfigurationPermissions.read(HgConfig.PERMISSION).check();

    HgConfig config = repositoryHandler.getConfig();

    if (config == null) {
      config = new HgConfig();
      repositoryHandler.setConfig(config);
    }

    return Response.ok(configToDtoMapper.map(config)).build();
  }

  /**
   * Modifies the hg config.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @Consumes(HgVndMediaType.CONFIG)
  @Operation(summary = "Modify hg configuration", description = "Modifies the global mercurial configuration.", tags = "Mercurial", operationId = "hg_put_config")
  @ApiResponse(
    responseCode = "204",
    description = "update success"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:hg\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response update(HgConfigDto configDto) {

    HgConfig config = dtoToConfigMapper.map(configDto);

    ConfigurationPermissions.write(config).check();

    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }

  @Path("packages")
  public HgConfigPackageResource getPackagesResource() {
    return packagesResource.get();
  }

  @Path("auto-configuration")
  public HgConfigAutoConfigurationResource getAutoConfigurationResource() {
    return autoconfigResource.get();
  }

  @Path("installations")
  public HgConfigInstallationsResource getInstallationsResource() {
    return installationsResource.get();
  }
}
