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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.VndMediaType;

import static sonia.scm.api.v2.resources.HgConfigResource.HG_CONFIG_PATH_V2;

@Path(HG_CONFIG_PATH_V2 + "/auto-configuration")
public class HgGlobalConfigAutoConfigurationResource {

  private final HgRepositoryHandler repositoryHandler;
  private final HgGlobalConfigDtoToHgConfigMapper dtoToConfigMapper;

  @Inject
  public HgGlobalConfigAutoConfigurationResource(HgGlobalConfigDtoToHgConfigMapper dtoToConfigMapper,
                                                 HgRepositoryHandler repositoryHandler) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.repositoryHandler = repositoryHandler;
  }

  /**
   * Sets the default hg config and installs the hg binary.
   */
  @PUT
  @Path("")
  @Operation(summary = "Sets hg configuration and installs hg binary", description = "Sets the default mercurial config and installs the mercurial binary.", tags = "Mercurial")
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
  public Response autoConfigurationWithoutDto() {
    return autoConfiguration(null);
  }

  /**
   * Modifies the hg config and installs the hg binary.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @Consumes(HgVndMediaType.CONFIG)
  @Operation(
    summary = "Modifies hg configuration and installs hg binary",
    description = "Modifies the mercurial config and installs the mercurial binary.",
    tags = "Mercurial",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = HgVndMediaType.CONFIG,
        schema = @Schema(implementation = UpdateHgGlobalConfigDto.class),
        examples = @ExampleObject(
          name = "Overwrites current configuration with this one and installs the mercurial binary.",
          value = "{\n  \"disabled\":false,\n  \"hgBinary\":\"hg\",\n  \"pythonBinary\":\"python\",\n  \"pythonPath\":\"\",\n  \"encoding\":\"UTF-8\",\n  \"useOptimizedBytecode\":false,\n  \"showRevisionInId\":false,\n  \"disableHookSSLValidation\":false,\n  \"enableHttpPostArgs\":false\n}",
          summary = "Simple update configuration and installs binary"
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
  public Response autoConfiguration(HgGlobalGlobalConfigDto configDto) {

    HgGlobalConfig config;

    if (configDto != null) {
      config = dtoToConfigMapper.map(configDto);
    } else {
      config = new HgGlobalConfig();
    }

    ConfigurationPermissions.write(config).check();

    repositoryHandler.doAutoConfiguration(config);
    HgGlobalConfig oldConfig = repositoryHandler.getConfig();
    oldConfig.setHgBinary(config.getHgBinary());
    repositoryHandler.setConfig(oldConfig);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }
}
