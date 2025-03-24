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

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.config.SecureKeyService;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.NamespaceStrategyValidator;
import sonia.scm.util.JsonMerger;
import sonia.scm.web.VndMediaType;

/**
 * RESTful Web Service Resource to manage the configuration.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Instance configuration", description = "Global SCM-Manager instance configuration")
})
@Path(ConfigResource.CONFIG_PATH_V2)
public class ConfigResource {

  static final String CONFIG_PATH_V2 = "v2/config";
  private final ConfigDtoToScmConfigurationMapper dtoToConfigMapper;
  private final ScmConfigurationToConfigDtoMapper configToDtoMapper;
  private final ScmConfigurationStore store;

  private final NamespaceStrategyValidator namespaceStrategyValidator;
  private final JsonMerger jsonMerger;
  private final SecureKeyService secureKeyService;


  @Inject
  public ConfigResource(ScmConfigurationStore store, ConfigDtoToScmConfigurationMapper dtoToConfigMapper,
                        ScmConfigurationToConfigDtoMapper configToDtoMapper,
                        NamespaceStrategyValidator namespaceStrategyValidator,
                        JsonMerger jsonMerger,
                        SecureKeyService secureKeyService) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.store = store;
    this.namespaceStrategyValidator = namespaceStrategyValidator;
    this.jsonMerger = jsonMerger;
    this.secureKeyService = secureKeyService;
  }

  /**
   * Returns the global scm config.
   */
  @GET
  @Path("")
  @Produces(VndMediaType.CONFIG)
  @Operation(summary = "Instance configuration", description = "Returns the instance configuration.", tags = "Instance configuration")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.CONFIG,
      schema = @Schema(implementation = ConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response get() {
    // We do this permission check in Resource and not in ScmConfiguration, because it must be available for reading
    // from within the code (plugins, etc.), but not for the whole anonymous world outside.
    ScmConfiguration scmConfiguration = store.get();
    ConfigurationPermissions.read(scmConfiguration).check();

    return Response.ok(configToDtoMapper.map(scmConfiguration)).build();
  }

  /**
   * Modifies the global scm config.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.CONFIG)
  @Operation(
    summary = "Update instance configuration",
    description = "Modifies the instance configuration.",
    tags = "Instance configuration",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.CONFIG,
        schema = @Schema(implementation = UpdateConfigDto.class),
        examples = @ExampleObject(
          name = "Overwrites current configuration with this one.",
          value = "{\n  \"realmDescription\":\"SONIA :: SCM-Manager\",\n  \"dateFormat\":\"YYYY-MM-DD HH:mm:ss\",\n  \"baseUrl\":\"http://localhost:8081/scm\",\n  \"loginAttemptLimit\":-1,\n  \"pluginUrl\":\"https://plugin-center-api.scm-manager.org/api/v1/plugins/{version}?os={os}&arch={arch}\",\n  \"loginAttemptLimitTimeout\":500,\n  \"namespaceStrategy\":\"CustomNamespaceStrategy\",\n  \"loginInfoUrl\":\"https://login-info.scm-manager.org/api/v1/login-info\",\n  \"releaseFeedUrl\":\"https://scm-manager.org/download/rss.xml\",\n  \"mailDomainName\":\"scm-manager.local\"\n}",
          summary = "Simple update configuration"
        )
      )
    )
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response update(@Valid ConfigDto configDto) {
    ScmConfiguration scmConfiguration = store.get();
    ConfigurationPermissions.write(scmConfiguration).check();
    updateConfig(configDto);

    return Response.noContent().build();
  }

  /**
   * Modifies the global scm config partially.
   *
   * @param updateNode json object which contains changed fields
   */
  @PATCH
  @Path("")
  @Consumes(VndMediaType.CONFIG)
  @Operation(
    summary = "Update instance configuration partially",
    description = "Modifies the instance configuration partially.",
    tags = "Instance configuration",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.CONFIG,
        schema = @Schema(implementation = UpdateConfigDto.class),
        examples = @ExampleObject(
          name = "Overwrites the provided fields of the current configuration.",
          value = "{\n  \"realmDescription\":\"SCM-Manager Realm\" \n}",
          summary = "Update configuration partially"
        )
      )
    )
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response updatePartially(JsonNode updateNode) {
    ScmConfiguration scmConfiguration = store.get();
    ConfigurationPermissions.write(scmConfiguration).check();

    ConfigDto updatedConfigDto = jsonMerger
      .fromObject(configToDtoMapper.map(scmConfiguration))
      .mergeWithJson(updateNode)
      .toObject(ConfigDto.class)
      .withValidation()
      .build();
    updateConfig(updatedConfigDto);

    return Response.noContent().build();
  }

  private void updateConfig(ConfigDto updatedConfigDto) {
    // ensure the namespace strategy is valid
    namespaceStrategyValidator.check(updatedConfigDto.getNamespaceStrategy());
    if (store.get().getJwtExpirationInH() > updatedConfigDto.getJwtExpirationInH()) {
      secureKeyService.clearAllTokens();
    }
    store.store(dtoToConfigMapper.map(updatedConfigDto));
  }
}
