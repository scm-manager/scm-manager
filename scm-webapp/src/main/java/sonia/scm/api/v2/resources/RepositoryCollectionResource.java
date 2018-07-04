package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public class RepositoryCollectionResource {

  private final CollectionResourceManagerAdapter<Repository, RepositoryDto, RepositoryException> adapter;
  private final RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper;

  @Inject
  public RepositoryCollectionResource(RepositoryManager manager, RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper) {
    this.adapter = new CollectionResourceManagerAdapter<>(manager);
    this.repositoryCollectionToDtoMapper = repositoryCollectionToDtoMapper;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_COLLECTION)
  @TypeHint(UserDto[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("10") @QueryParam("pageSize") int pageSize,
    @QueryParam("sortBy") String sortBy,
    @DefaultValue("false") @QueryParam("desc") boolean desc) {
    return adapter.getAll(page, pageSize, sortBy, desc,
      pageResult -> repositoryCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  @POST
  @Path("")
  public Response create() {
    throw new UnsupportedOperationException();
  }
}
