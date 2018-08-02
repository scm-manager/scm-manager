package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class RepositoryTypeCollectionResource {

  private RepositoryManager repositoryManager;
  private RepositoryTypeCollectionToDtoMapper mapper;

  @Inject
  public RepositoryTypeCollectionResource(RepositoryManager repositoryManager, RepositoryTypeCollectionToDtoMapper mapper) {
    this.repositoryManager = repositoryManager;
    this.mapper = mapper;
  }

  @GET
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.REPOSITORY_TYPE_COLLECTION)
  public HalRepresentation getAll() {
    return mapper.map(repositoryManager.getConfiguredTypes());
  }

}
