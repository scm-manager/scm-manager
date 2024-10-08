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

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.web.GitVndMediaType;
import sonia.scm.web.VndMediaType;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitRepositoryConfigResource {

  private static final Logger LOG = LoggerFactory.getLogger(GitRepositoryConfigResource.class);

  private final GitRepositoryConfigMapper repositoryConfigMapper;
  private final RepositoryManager repositoryManager;
  private final GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider;
  private final GitRepositoryHandler repositoryHandler;

  @Inject
  public GitRepositoryConfigResource(GitRepositoryConfigMapper repositoryConfigMapper, RepositoryManager repositoryManager, GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider, GitRepositoryHandler repositoryHandler) {
    this.repositoryConfigMapper = repositoryConfigMapper;
    this.repositoryManager = repositoryManager;
    this.gitRepositoryConfigStoreProvider = gitRepositoryConfigStoreProvider;
    this.repositoryHandler = repositoryHandler;
  }

  @GET
  @Path("/")
  @Produces(GitVndMediaType.GIT_REPOSITORY_CONFIG)
  @Operation(summary = "Git repository configuration", description = "Returns the repository related git configuration.", tags = "Git")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = GitVndMediaType.GIT_REPOSITORY_CONFIG,
      schema = @Schema(implementation = GitRepositoryConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the repository config")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
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
  public Response getRepositoryConfig(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = getRepository(namespace, name);
    RepositoryPermissions.read(repository).check();
    GitRepositoryConfig config = getStore(repository).get();
    GitRepositoryConfigDto dto = repositoryConfigMapper.map(config, repository);
    return Response.ok(dto).build();
  }

  @GET
  @Path("default-branch")
  @Produces(GitVndMediaType.GIT_REPOSITORY_DEFAULT_BRANCH)
  @Operation(summary = "Git repository default branch", description = "Returns the default branch for the repository.", tags = "Git")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = GitVndMediaType.GIT_REPOSITORY_CONFIG,
      schema = @Schema(implementation = GitRepositoryConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the repository config")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
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
  public Response getDefaultBranch(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = getRepository(namespace, name);
    RepositoryPermissions.read(repository).check();
    GitRepositoryConfig config = getStore(repository).get();

    String defaultBranch = "main";

    if (!Strings.isNullOrEmpty(config.getDefaultBranch())) {
      defaultBranch = config.getDefaultBranch();
    } else if (!Strings.isNullOrEmpty(repositoryHandler.getConfig().getDefaultBranch())) {
      defaultBranch = repositoryHandler.getConfig().getDefaultBranch();
    }

    return Response.ok(new DefaultBranchDto(defaultBranch)).build();
  }

  @PUT
  @Path("/")
  @Consumes(GitVndMediaType.GIT_REPOSITORY_CONFIG)
  @Operation(
    summary = "Modifies git repository configuration",
    description = "Modifies the repository related git configuration.",
    tags = "Git",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = GitVndMediaType.GIT_REPOSITORY_CONFIG,
        schema = @Schema(implementation = UpdateGitRepositoryConfigDto.class),
        examples = @ExampleObject(
          name = "Overwrites current configuration with this one.",
          value = "{\n  \"defaultBranch\":\"main\"\n  \"nonFastForwardDisallowed\":false,\n}",
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
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege to change this repositories config")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available/name available",
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
  public Response setRepositoryConfig(@PathParam("namespace") String namespace, @PathParam("name") String name, GitRepositoryConfigDto dto) {
    Repository repository = getRepository(namespace, name);
    RepositoryPermissions.custom("git", repository).check();
    ConfigurationStore<GitRepositoryConfig> repositoryConfigStore = getStore(repository);
    GitRepositoryConfig config = repositoryConfigMapper.map(dto);
    repositoryConfigStore.set(config);
    LOG.info("git default branch of repository {} has changed, sending clear cache event", repository);
    return Response.noContent().build();
  }

  private Repository getRepository(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      throw notFound(entity(namespaceAndName));
    }
    return repository;
  }

  private ConfigurationStore<GitRepositoryConfig> getStore(Repository repository) {
    return gitRepositoryConfigStoreProvider.get(repository);
  }

  @Getter
  @AllArgsConstructor
  public static class DefaultBranchDto {
    private final String defaultBranch;
  }
}
