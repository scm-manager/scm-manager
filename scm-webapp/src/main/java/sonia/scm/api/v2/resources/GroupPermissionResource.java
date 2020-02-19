package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
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
  @ApiResponse(responseCode = "404", description = "not found, no group with the specified id/name available")
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
  @Operation(summary = "Update Group permissions", description = "Sets permissions for a group. Overwrites all existing permissions.", tags = {"Group", "Permissions"})
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "Invalid body")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current group does not have the correct privilege")
  @ApiResponse(responseCode = "404", description = "not found, no group with the specified id/name available")
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
