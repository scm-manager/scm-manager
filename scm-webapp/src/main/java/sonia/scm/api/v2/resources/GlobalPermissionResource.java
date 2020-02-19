package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(tags = {
  @Tag(name = "Permissions", description = "Permission related endpoints")
})
@Path("v2/permissions")
public class GlobalPermissionResource {

  private PermissionAssigner permissionAssigner;

  @Inject
  public GlobalPermissionResource(PermissionAssigner permissionAssigner) {
    this.permissionAssigner = permissionAssigner;
  }

  @GET
  @Produces(VndMediaType.PERMISSION_COLLECTION)
  @Operation(summary = "List of permissions", description = "Returns all available permissions", tags = "Permissions")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.PERMISSION_COLLECTION,
      schema = @Schema(implementation = PermissionListDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the permissions")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @Path("")
  public Response getAll() {
    String[] permissions = permissionAssigner.getAvailablePermissions().stream().map(PermissionDescriptor::getValue).toArray(String[]::new);
    return Response.ok(new PermissionListDto(permissions)).build();
  }
}
