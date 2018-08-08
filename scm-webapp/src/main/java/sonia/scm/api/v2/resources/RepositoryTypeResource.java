package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class RepositoryTypeResource {

  private RepositoryManager repositoryManager;
  private RepositoryTypeToRepositoryTypeDtoMapper mapper;

  @Inject
  public RepositoryTypeResource(RepositoryManager repositoryManager, RepositoryTypeToRepositoryTypeDtoMapper mapper) {
    this.repositoryManager = repositoryManager;
    this.mapper = mapper;
  }

  /**
   * Returns the specified repository type.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param name of the requested repository type
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_TYPE)
  @TypeHint(RepositoryTypeDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 404, condition = "not found, no repository type with the specified name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("name") String name) {
    for (RepositoryType type : repositoryManager.getConfiguredTypes()) {
      if (name.equalsIgnoreCase(type.getName())) {
        return Response.ok(mapper.map(type)).build();
      }
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

}
