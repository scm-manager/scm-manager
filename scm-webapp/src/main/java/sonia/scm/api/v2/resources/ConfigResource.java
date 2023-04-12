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

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.NamespaceStrategyValidator;
import sonia.scm.util.JsonMerger;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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


  @Inject
  public ConfigResource(ScmConfigurationStore store, ConfigDtoToScmConfigurationMapper dtoToConfigMapper,
                        ScmConfigurationToConfigDtoMapper configToDtoMapper,
                        NamespaceStrategyValidator namespaceStrategyValidator,
                        JsonMerger jsonMerger) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.store = store;
    this.namespaceStrategyValidator = namespaceStrategyValidator;
    this.jsonMerger = jsonMerger;
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
    store.store(dtoToConfigMapper.map(updatedConfigDto));
  }
}
