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
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

public class GroupResource {

  private final GroupToGroupDtoMapper groupToGroupDtoMapper;
  private final GroupDtoToGroupMapper dtoToGroupMapper;
  private final IdResourceManagerAdapter<Group, GroupDto> adapter;
  private final GroupPermissionResource groupPermissionResource;

  @Inject
  public GroupResource(GroupManager manager, GroupToGroupDtoMapper groupToGroupDtoMapper,
                       GroupDtoToGroupMapper groupDtoToGroupMapper, GroupPermissionResource groupPermissionResource) {
    this.groupToGroupDtoMapper = groupToGroupDtoMapper;
    this.dtoToGroupMapper = groupDtoToGroupMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, Group.class);
    this.groupPermissionResource = groupPermissionResource;
  }

  /**
   * Returns a group.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param id the id/name of the group
   */
  @GET
  @Path("")
  @Produces(VndMediaType.GROUP)
  @Operation(summary = "Get single group", description = "Returns a group.", tags = "Group")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.GROUP,
      schema = @Schema(implementation = GroupDto.class)
    )
  )
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
  public Response get(@PathParam("id") String id) {
    return adapter.get(id, groupToGroupDtoMapper::map);
  }

  /**
   * Deletes a group.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param name the name of the group to delete.
   */
  @DELETE
  @Path("")
  @Operation(summary = "Delete group", description = "Deletes the group with the given id.", tags = "Group")
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the group")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response delete(@PathParam("id") String name) {
    return adapter.delete(name);
  }

  /**
   * Modifies the given group.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param name  name of the group to be modified
   * @param group group object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.GROUP)
  @Operation(
    summary = "Update group",
    description = "Modifies a group.",
    tags = "Group",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.GROUP,
        schema = @Schema(implementation = UpdateGroupDto.class),
        examples = @ExampleObject(
          name = "Update a group description",
          value = "{\n  \"name\":\"manager\",\n  \"description\":\"Group of managers with full read access\",\n  \"lastModified\":\"2020-06-05T14:42:49.000Z\",\n  \"type\":\"xml\"\n}",
          summary = "Update a group"
        )
      )
    )
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. illegal change of id/group name")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"group\" privilege")
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
  public Response update(@PathParam("id") String name, @Valid GroupDto group) {
    return adapter.update(name, existing -> dtoToGroupMapper.map(group));
  }

  @Path("permissions")
  public GroupPermissionResource permissions() {
    return groupPermissionResource;
  }

}
