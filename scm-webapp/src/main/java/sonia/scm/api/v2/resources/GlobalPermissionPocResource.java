package sonia.scm.api.v2.resources;

import lombok.extern.slf4j.Slf4j;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.SecuritySystem;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Global Permission Proof of Concept (POC).
 * TODO Extend or delete this during implementation!
 */
@Path("v2/permissions")
@Slf4j
public class GlobalPermissionPocResource {

  private SecuritySystem securitySystem;

  @Inject
  public GlobalPermissionPocResource(SecuritySystem securitySystem) {
    this.securitySystem = securitySystem;
  }


  /**
   
    How to use this proof of concept?
   
      curl -vu scmadmin:scmadmin --data '{
      "active": true,
      "admin": false,
      "displayName": "arthur",
      "mail": "x@abcde.cd",
      "name": "arthur",
      "password": "scmadmin",
      "type": "xml"
      }' \
       --header "Content-Type: application/vnd.scmm-user+json;v=2"  http://localhost:8081/scm/api/v2/users/
   
       curl -vu scmadmin:scmadmin --data '{
      "description": "descr",
      "name": "configurers",
      "members": [ "arthur" ]
     }' \
      --header "Content-Type: application/vnd.scmm-group+json" http://localhost:8081/scm/api/v2/groups/
   
    # not allowed
    curl -vu arthur:scmadmin http://localhost:8081/scm/api/v2/config
    # not allowed (empty)
    curl -vu arthur:scmadmin "http://localhost:8081/scm/api/v2/groups/?sortBy=name&desc=true" | jq
   
    # Assign permissions (call this resource)
    curl -X POST  -vu scmadmin:scmadmin http://localhost:8081/scm/api/v2/permissions
   
    # Now allowed via individual permission
    curl -vu arthur:scmadmin "http://localhost:8081/scm/api/v2/groups/?sortBy=name&desc=true" | jq
    # allowed via group permission
    curl -vu arthur:scmadmin http://localhost:8081/scm/api/v2/config | jq
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("")
  public Response create() {

    // Should contain all permissions defined in permissions.xmls on the classpath.
    // Core: scm-webapp/src/main/resources/META-INF/scm/permissions.xml
    // Plugins, e.g. scm-plugins/scm-git-plugin/src/main/resources/META-INF/scm/permissions.xml
    log.info("{} Available permissions: {}", securitySystem.getAvailablePermissions().size(), securitySystem.getAvailablePermissions());

    assignExemplaryPermissions();

    // TODO use created()
    return Response.noContent().build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("")
  public Response getAll() {
    String[] permissions = securitySystem.getAvailablePermissions().stream().map(PermissionDescriptor::getValue).toArray(String[]::new);
    return Response.ok(new PerminssionListDto(permissions)).build();
  }

  protected void assignExemplaryPermissions() {
    AssignedPermission groupPermission = new AssignedPermission("configurers", true,"configuration:*");
    log.info("try to add new permission: {}", groupPermission);
    securitySystem.addPermission(groupPermission);

    AssignedPermission userPermission = new AssignedPermission("rene", "group:*");
    log.info("try to add new permission: {}", userPermission);
    securitySystem.addPermission(userPermission);
  }
}


