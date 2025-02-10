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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgVerifier;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.VndMediaType;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

/**
 * RESTful Web Service Resource to manage the configuration of the hg plugin.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Mercurial", description = "Configuration for the mercurial repository type")
})
@Path(HgConfigResource.HG_CONFIG_PATH_V2)
public class HgConfigResource {

  static final String HG_CONFIG_PATH_V2 = "v2/config/hg";
  private final HgGlobalConfigDtoToHgConfigMapper dtoToConfigMapper;
  private final HgGlobalConfigToHgGlobalConfigDtoMapper configToDtoMapper;
  private final HgRepositoryHandler repositoryHandler;
  private final Provider<HgGlobalConfigAutoConfigurationResource> autoconfigResource;
  private final Provider<HgRepositoryConfigResource> repositoryConfigResource;

  @Inject
  public HgConfigResource(HgGlobalConfigDtoToHgConfigMapper dtoToConfigMapper,
                          HgGlobalConfigToHgGlobalConfigDtoMapper configToDtoMapper,
                          HgRepositoryHandler repositoryHandler,
                          Provider<HgGlobalConfigAutoConfigurationResource> autoconfigResource,
                          Provider<HgRepositoryConfigResource> repositoryConfigResource) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.repositoryHandler = repositoryHandler;
    this.autoconfigResource = autoconfigResource;
    this.repositoryConfigResource = repositoryConfigResource;
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
      schema = @Schema(implementation = HgGlobalGlobalConfigDto.class)
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

    ConfigurationPermissions.read(HgGlobalConfig.PERMISSION).check();

    HgGlobalConfig config = repositoryHandler.getConfig();

    if (config == null) {
      config = new HgGlobalConfig();
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
  @Operation(
    summary = "Modify hg configuration",
    description = "Modifies the global mercurial configuration.",
    tags = "Mercurial",
    operationId = "hg_put_config",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = HgVndMediaType.CONFIG,
        schema = @Schema(implementation = UpdateHgGlobalConfigDto.class),
        examples = @ExampleObject(
          name = "Overwrites current configuration with this one.",
          value = "{\n  \"disabled\":false,\n  \"hgBinary\":\"hg\",\n  \"encoding\":\"UTF-8\",\n  \"showRevisionInId\":false,\n  \"enableHttpPostArgs\":false\n}",
          summary = "Simple update configuration"
        )
      )
    )
  )
  @ApiResponse(
    responseCode = "204",
    description = "update success"
  )
  @ApiResponse(responseCode = "400", description = "invalid configuration")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:hg\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response update(@Valid HgGlobalGlobalConfigDto configDto) {
    HgGlobalConfig config = dtoToConfigMapper.map(configDto);
    ConfigurationPermissions.write(config).check();

    if (!config.isDisabled() && config.getHgBinary() != null) {
      HgVerifier.HgVerifyStatus verifyStatus = new HgVerifier().verify(config.getHgBinary());
      doThrow()
        .violation(verifyStatus.getDescription())
        .when(verifyStatus != HgVerifier.HgVerifyStatus.VALID);
    }

    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }

  @Path("auto-configuration")
  public HgGlobalConfigAutoConfigurationResource getAutoConfigurationResource() {
    return autoconfigResource.get();
  }


  @Path("{namespace}/{name}")
  public HgRepositoryConfigResource getRepositoryConfigResource() {
    return repositoryConfigResource.get();
  }
}
