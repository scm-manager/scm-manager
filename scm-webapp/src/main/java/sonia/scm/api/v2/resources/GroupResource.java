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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
  @Operation(summary = "Update group", description = "Modifies a group.", tags = "Group")
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
