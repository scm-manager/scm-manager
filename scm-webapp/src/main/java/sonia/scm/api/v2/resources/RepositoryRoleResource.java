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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.web.VndMediaType;

public class RepositoryRoleResource {

  private final RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper;
  private final RepositoryRoleToRepositoryRoleDtoMapper repositoryRoleToDtoMapper;

  private final IdResourceManagerAdapter<RepositoryRole, RepositoryRoleDto> adapter;

  @Inject
  public RepositoryRoleResource(
    RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper,
    RepositoryRoleToRepositoryRoleDtoMapper repositoryRoleToDtoMapper,
    RepositoryRoleManager manager) {
    this.dtoToRepositoryRoleMapper = dtoToRepositoryRoleMapper;
    this.repositoryRoleToDtoMapper = repositoryRoleToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, RepositoryRole.class);
  }

  /**
   * Returns a repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param name the id/name of the repository role
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_ROLE)
  @Operation(summary = "Get single repository role", description = "Returns the repository role for the given name.", tags = "Repository role")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_ROLE,
      schema = @Schema(implementation = RepositoryRoleDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the repository role")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository role with the specified name available",
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
  public Response get(@PathParam("name") String name) {
    return adapter.get(name, repositoryRoleToDtoMapper::map);
  }

  /**
   * Deletes a repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param name the name of the repository role to delete.
   */
  @DELETE
  @Path("")
  @Operation(summary = "Delete repository role", description = "Deletes the repository role with the given name.", tags = "Repository role")
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repositoryRole\" privilege")
  @ApiResponse(responseCode = "500", description = "internal server error")
  public Response delete(@PathParam("name") String name) {
    return adapter.delete(name);
  }

  /**
   * Modifies the given repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param name    name of the repository role to be modified
   * @param repositoryRole repository role object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.REPOSITORY_ROLE)
  @Operation(
    summary = "Update repository role",
    description = "Modifies the repository role for the given name.",
    tags = "Repository role",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.REPOSITORY_ROLE,
        schema = @Schema(implementation = UpdateRepositoryRoleDto.class),
        examples = @ExampleObject(
          name = "Update repository role named hero with this verbs.",
          value = "{\n  \"name\":\"hero\",\n  \"system\":false,\n  \"verbs\":[\"read\",\"pull\",\"write\",\"push\",\"delete\"],\n  \"lastModified\":\"2020-06-05T14:42:49.000Z\"\n}",
          summary = "Update a repository role"
        )
      )
    )
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. illegal change of repository role name")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repositoryRole\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository role with the specified name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public Response update(@PathParam("name") String name, @Valid RepositoryRoleDto repositoryRole) {
    return adapter.update(name, existing -> dtoToRepositoryRoleMapper.map(repositoryRole));
  }

}
