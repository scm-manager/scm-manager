package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
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
  @Operation(summary = "Update user permissions", description = "Sets permissions for a user. Overwrites all existing permissions.", tags = {"User", "Permissions"})
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the correct privilege")
  @ApiResponse(responseCode = "404", description = "not found, no user with the specified id/name available")
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
