package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class RepositoryResource {

  private final RepositoryToRepositoryDtoMapper repositoryToDtoMapper;

  private final RepositoryManager manager;
  private final SingleResourceManagerAdapter<Repository, RepositoryDto, RepositoryException> adapter;
  private final Provider<TagRootResource> tagRootResource;
  private final Provider<BranchRootResource> branchRootResource;

  @Inject
  public RepositoryResource(
    RepositoryToRepositoryDtoMapper repositoryToDtoMapper,
    RepositoryManager manager,
    Provider<TagRootResource> tagRootResource,
    Provider<BranchRootResource> branchRootResource) {
    this.manager = manager;
    this.repositoryToDtoMapper = repositoryToDtoMapper;
    this.adapter = new SingleResourceManagerAdapter<>(manager);
    this.tagRootResource = tagRootResource;
    this.branchRootResource = branchRootResource;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY)
  @TypeHint(RepositoryDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the repository"),
    @ResponseCode(code = 404, condition = "not found, no repository with the specified name available in the namespace"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return adapter.get(() -> manager.getByNamespace(namespace, name), repositoryToDtoMapper::map);
  }

  @DELETE
  @Path("")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    throw new UnsupportedOperationException();
  }

  @PUT
  @Path("")
  public Response update(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    throw new UnsupportedOperationException();
  }

  @Path("tags/")
  public TagRootResource tags() {
    return tagRootResource.get();
  }

  @Path("branches/")
  public BranchRootResource branches() {
    return branchRootResource.get();
  }
}
