package sonia.scm.api.v2.resources;

import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("v2/permissions")
public class GlobalPermissionResource {

  private PermissionAssigner permissionAssigner;

  @Inject
  public GlobalPermissionResource(PermissionAssigner permissionAssigner) {
    this.permissionAssigner = permissionAssigner;
  }

  @GET
  @Produces(VndMediaType.PERMISSION_COLLECTION)
  @Path("")
  public Response getAll() {
    String[] permissions = permissionAssigner.getAvailablePermissions().stream().map(PermissionDescriptor::getValue).toArray(String[]::new);
    return Response.ok(new PermissionListDto(permissions)).build();
  }
}
