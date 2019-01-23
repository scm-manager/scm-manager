package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;

/**
 * RESTful Web Service Resource to get available repository types.
 */
@Path(RepositoryPermissionResource.PATH)
public class RepositoryPermissionResource {

  static final String PATH = "v2/repositoryPermissions/";

  private final RepositoryPermissionProvider repositoryPermissionProvider;

  @Inject
  public RepositoryPermissionResource(RepositoryPermissionProvider repositoryPermissionProvider) {
    this.repositoryPermissionProvider = repositoryPermissionProvider;
  }

  @GET
  @Path("verbs")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.REPOSITORY_TYPE_COLLECTION)
  public Collection<String> getRepositoryPermissionVerbs() {
    return repositoryPermissionProvider.availableVerbs();
  }

  @GET
  @Path("roles")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.REPOSITORY_TYPE_COLLECTION)
  public Collection getRepositoryRoles() {
    return repositoryPermissionProvider.availableRoles();
  }
}
