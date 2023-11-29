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
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.SvnConfig;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.web.SvnVndMediaType;
import sonia.scm.web.VndMediaType;

/**
 * RESTful Web Service Resource to manage the configuration of the svn plugin.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Subversion", description = "Configuration for the subversion repository type")
})
@Path(SvnConfigResource.SVN_CONFIG_PATH_V2)
public class SvnConfigResource {

  static final String SVN_CONFIG_PATH_V2 = "v2/config/svn";
  private final SvnConfigDtoToSvnConfigMapper dtoToConfigMapper;
  private final SvnConfigToSvnConfigDtoMapper configToDtoMapper;
  private final SvnRepositoryHandler repositoryHandler;

  @Inject
  public SvnConfigResource(SvnConfigDtoToSvnConfigMapper dtoToConfigMapper, SvnConfigToSvnConfigDtoMapper configToDtoMapper,
                           SvnRepositoryHandler repositoryHandler) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.repositoryHandler = repositoryHandler;
  }

  /**
   * Returns the svn config.
   */
  @GET
  @Path("")
  @Produces(SvnVndMediaType.SVN_CONFIG)
  @Operation(summary = "Svn configuration", description = "Returns the global subversion configuration.", tags = "Subversion", operationId = "svn_get_config")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = SvnVndMediaType.SVN_CONFIG,
      schema = @Schema(implementation = SvnConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:svn\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response get() {

    SvnConfig config = repositoryHandler.getConfig();

    if (config == null) {
      config = new SvnConfig();
      repositoryHandler.setConfig(config);
    }

    ConfigurationPermissions.read(config).check();

    return Response.ok(configToDtoMapper.map(config)).build();
  }

  /**
   * Modifies the svn config.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @Consumes(SvnVndMediaType.SVN_CONFIG)
  @Operation(
    summary = "Modify svn configuration",
    description = "Modifies the global subversion configuration.",
    tags = "Subversion",
    operationId = "svn_put_config",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = SvnVndMediaType.SVN_CONFIG,
        schema = @Schema(implementation = UpdateSvnConfigDto.class),
        examples = @ExampleObject(
          name = "Overwrites current configuration with this one.",
          value = "{\n  \"disabled\":false,\n  \"compatibility\":\"NONE\",\n  \"enabledGZip\":false\n}",
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
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:svn\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response update(SvnConfigDto configDto) {

    SvnConfig config = dtoToConfigMapper.map(configDto);

    ConfigurationPermissions.write(config).check();

    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }

}
