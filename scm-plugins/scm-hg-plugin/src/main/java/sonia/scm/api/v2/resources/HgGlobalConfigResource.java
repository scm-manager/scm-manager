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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.HgGlobalConfig;
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
@Path(HgGlobalConfigResource.HG_CONFIG_PATH_V2)
public class HgGlobalConfigResource {

  static final String HG_CONFIG_PATH_V2 = "v2/config/hg";
  private final HgGlobalConfigDtoToHgConfigMapper dtoToConfigMapper;
  private final HgGlobalConfigToHgGlobalConfigDtoMapper configToDtoMapper;
  private final HgRepositoryHandler repositoryHandler;
  private final Provider<HgGlobalConfigAutoConfigurationResource> autoconfigResource;

  @Inject
  public HgGlobalConfigResource(HgGlobalConfigDtoToHgConfigMapper dtoToConfigMapper,
                                HgGlobalConfigToHgGlobalConfigDtoMapper configToDtoMapper,
                                HgRepositoryHandler repositoryHandler,
                                Provider<HgGlobalConfigAutoConfigurationResource> autoconfigResource) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.repositoryHandler = repositoryHandler;
    this.autoconfigResource = autoconfigResource;
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
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:hg\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response update(HgGlobalGlobalConfigDto configDto) {

    HgGlobalConfig config = dtoToConfigMapper.map(configDto);

    ConfigurationPermissions.write(config).check();

    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }

  @Path("auto-configuration")
  public HgGlobalConfigAutoConfigurationResource getAutoConfigurationResource() {
    return autoconfigResource.get();
  }
}
