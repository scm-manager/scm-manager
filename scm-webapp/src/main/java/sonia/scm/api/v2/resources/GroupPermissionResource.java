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
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.web.VndMediaType;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class GroupPermissionResource {

  private final PermissionAssigner permissionAssigner;
  private final PermissionCollectionToDtoMapper permissionCollectionToDtoMapper;

  @Inject
  public GroupPermissionResource(PermissionAssigner permissionAssigner, PermissionCollectionToDtoMapper permissionCollectionToDtoMapper) {
    this.permissionAssigner = permissionAssigner;
    this.permissionCollectionToDtoMapper = permissionCollectionToDtoMapper;
  }

  /**
   * Returns permissions for a group.
   *
   * @param id the id/name of the group
   */
  @GET
  @Path("")
  @Produces(VndMediaType.PERMISSION_COLLECTION)
  @Operation(summary = "Group permission", description = "Returns permissions for a group.", tags = {"Group", "Permissions"})
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.PERMISSION_COLLECTION,
      schema = @Schema(implementation = PermissionListDto.class)
    ))
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the group")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no group with the specified id/name available",
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
  public Response getPermissions(@PathParam("id") String id) {
    PermissionPermissions.read().check();
    Collection<PermissionDescriptor> permissions = permissionAssigner.readPermissionsForGroup(id);
    return Response.ok(permissionCollectionToDtoMapper.mapForGroup(permissions, id)).build();
  }

  /**
   * Sets permissions for a group. Overwrites all existing permissions.
   *
   * @param id             id of the group to be modified
   * @param newPermissions New list of permissions for the group
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.PERMISSION_COLLECTION)
  @Operation(
    summary = "Update Group permissions",
    description = "Sets permissions for a group. Overwrites all existing permissions.",
    tags = {"Group", "Permissions"},
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.PERMISSION_COLLECTION,
        schema = @Schema(implementation = PermissionListDto.class),
        examples = @ExampleObject(
          name = "Add read permissions for all repositories and pull requests",
          value = "{\n  \"permissions\":[\"repository:read,pull:*\",\"repository:readPullRequest:*\"]\n}",
          summary = "Simple update group permissions"
        )
      )
    )
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current group does not have the correct privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no group with the specified id/name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "409", description = "conflict, group has been modified concurrently")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response overwritePermissions(@PathParam("id") String id, PermissionListDto newPermissions) {
    Collection<PermissionDescriptor> permissionDescriptors = Arrays.stream(newPermissions.getPermissions())
      .map(PermissionDescriptor::new)
      .collect(Collectors.toList());
    permissionAssigner.setPermissionsForGroup(id, permissionDescriptors);
    return Response.noContent().build();
  }
}
