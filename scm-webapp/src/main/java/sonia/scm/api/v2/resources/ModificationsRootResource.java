package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class ModificationsRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final ModificationsToDtoMapper modificationsToDtoMapper;

  @Inject
  public ModificationsRootResource(RepositoryServiceFactory serviceFactory, ModificationsToDtoMapper modificationsToDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.modificationsToDtoMapper = modificationsToDtoMapper;
  }

  /**
   * Get the file modifications related to a revision.
   * file modifications are for example: Modified, Added or Removed.
   */
  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the modifications"),
    @ResponseCode(code = 404, condition = "not found, no changeset with the specified id is available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.MODIFICATIONS)
  @TypeHint(ModificationsDto.class)
  @Path("{revision}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Modifications modifications = repositoryService.getModificationsCommand()
        .revision(revision)
        .getModifications();
      ModificationsDto output = modificationsToDtoMapper.map(modifications, repositoryService.getRepository());
      if (modifications != null ) {
        return Response.ok(output).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    }
  }
}
