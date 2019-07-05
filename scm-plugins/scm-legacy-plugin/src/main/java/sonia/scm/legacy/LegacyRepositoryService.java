package sonia.scm.legacy;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("v2/legacy/repository")
public class LegacyRepositoryService {

  private RepositoryManager repositoryManager;

  @Inject
  public LegacyRepositoryService(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository:read:global\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public NamespaceAndNameDto getNameAndNamespaceForRepositoryId(@PathParam("id") String repositoryId) {
    Repository repo = repositoryManager.get(repositoryId);
    if (repo == null) {
      throw new NotFoundException(Repository.class, repositoryId);
    }
    return new NamespaceAndNameDto(repo.getName(), repo.getNamespace());
  }

}

