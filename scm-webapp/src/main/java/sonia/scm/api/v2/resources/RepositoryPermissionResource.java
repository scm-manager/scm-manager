package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import de.otto.edison.hal.Links;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * RESTful Web Service Resource to get available repository types.
 */
@Path(RepositoryPermissionResource.PATH)
public class RepositoryPermissionResource {

  static final String PATH = "v2/repositoryPermissions/";

  private final RepositoryPermissionProvider repositoryPermissionProvider;
  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryPermissionResource(RepositoryPermissionProvider repositoryPermissionProvider, ResourceLinks resourceLinks) {
    this.repositoryPermissionProvider = repositoryPermissionProvider;
    this.resourceLinks = resourceLinks;
  }

  @GET
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.REPOSITORY_PERMISSION_COLLECTION)
  public AvailableRepositoryPermissionsDto get() {
    AvailableRepositoryPermissionsDto dto = new AvailableRepositoryPermissionsDto(repositoryPermissionProvider.availableVerbs(), repositoryPermissionProvider.availableRoles());
    dto.add(Links.linkingTo().self(resourceLinks.availableRepositoryPermissions().self()).build());
    return dto;
  }
}
