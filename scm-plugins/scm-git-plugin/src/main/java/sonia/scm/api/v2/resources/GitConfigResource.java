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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.web.GitVndMediaType;
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
 * RESTful Web Service Resource to manage the configuration of the git plugin.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Git", description = "Configuration for the git repository type")
})
@Path(GitConfigResource.GIT_CONFIG_PATH_V2)
public class GitConfigResource {

  static final String GIT_CONFIG_PATH_V2 = "v2/config/git";
  private final GitConfigDtoToGitConfigMapper dtoToConfigMapper;
  private final GitConfigToGitConfigDtoMapper configToDtoMapper;
  private final GitRepositoryHandler repositoryHandler;
  private final Provider<GitRepositoryConfigResource> gitRepositoryConfigResource;

  @Inject
  public GitConfigResource(GitConfigDtoToGitConfigMapper dtoToConfigMapper, GitConfigToGitConfigDtoMapper configToDtoMapper,
                           GitRepositoryHandler repositoryHandler, Provider<GitRepositoryConfigResource> gitRepositoryConfigResource) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.repositoryHandler = repositoryHandler;
    this.gitRepositoryConfigResource = gitRepositoryConfigResource;
  }

  /**
   * Returns the git config.
   */
  @GET
  @Path("")
  @Produces(GitVndMediaType.GIT_CONFIG)
  @Operation(summary = "Git configuration", description = "Returns the global git configuration.", tags = "Git", operationId = "git_get_config")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = GitVndMediaType.GIT_CONFIG,
      schema = @Schema(implementation = GitConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:git\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response get() {

    GitConfig config = repositoryHandler.getConfig();

    if (config == null) {
      config = new GitConfig();
      repositoryHandler.setConfig(config);
    }

    ConfigurationPermissions.read(config).check();

    return Response.ok(configToDtoMapper.map(config)).build();
  }

  /**
   * Modifies the git config.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @Consumes(GitVndMediaType.GIT_CONFIG)
  @Operation(summary = "Modify git configuration", description = "Modifies the global git configuration.", tags = "Git", operationId = "git_put_config")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:git\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response update(GitConfigDto configDto) {

    GitConfig config = dtoToConfigMapper.map(configDto);

    ConfigurationPermissions.write(config).check();

    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }

  @Path("{namespace}/{name}")
  public GitRepositoryConfigResource getRepositoryConfig() {
    return gitRepositoryConfigResource.get();
  }
}
