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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.web.VndMediaType;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class UserPermissionResource {

  private final PermissionAssigner permissionAssigner;
  private final PermissionCollectionToDtoMapper permissionCollectionToDtoMapper;

  @Inject
  public UserPermissionResource(PermissionAssigner permissionAssigner, PermissionCollectionToDtoMapper permissionCollectionToDtoMapper) {
    this.permissionAssigner = permissionAssigner;
    this.permissionCollectionToDtoMapper = permissionCollectionToDtoMapper;
  }

  /**
   * Returns permissions for a user.
   *
   * @param id the id/name of the user
   */
  @GET
  @Path("")
  @Produces(VndMediaType.PERMISSION_COLLECTION)
  @Operation(summary = "User permission", description = "Returns the global git configuration.", tags = {"User", "Permissions"})
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.PERMISSION_COLLECTION,
      schema = @Schema(implementation = PermissionListDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the user")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no user with the specified id/name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response getPermissions(@PathParam("id") String id) {
    PermissionPermissions.read().check();
    Collection<PermissionDescriptor> permissions = permissionAssigner.readPermissionsForUser(id);
    return Response.ok(permissionCollectionToDtoMapper.mapForUser(permissions, id)).build();
  }

  /**
   * Sets permissions for a user. Overwrites all existing permissions.
   *
   * @param id             id of the user to be modified
   * @param newPermissions New list of permissions for the user
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.PERMISSION_COLLECTION)
  @Operation(
    summary = "Update user permissions",
    description = "Sets permissions for a user. Overwrites all existing permissions.",
    tags = {"User", "Permissions"},
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.PERMISSION_COLLECTION,
        schema = @Schema(implementation = UpdatePermissionListDto.class),
        examples = @ExampleObject(
          name = "Add read permissions for all repositories and pull requests.",
          value = "{\n  \"permissions\":[\"repository:read,pull:*\",\"repository:readPullRequest:*\"]\n}",
          summary = "Simple update user permissions"
        )
      )
    )
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the correct privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no user with the specified id/name available",
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
    )
  )
  public Response overwritePermissions(@PathParam("id") String id, @Valid PermissionListDto newPermissions) {
    Collection<PermissionDescriptor> permissionDescriptors = Arrays.stream(newPermissions.getPermissions())
      .map(PermissionDescriptor::new)
      .collect(Collectors.toList());
    permissionAssigner.setPermissionsForUser(id, permissionDescriptors);
    return Response.noContent().build();
  }
}
