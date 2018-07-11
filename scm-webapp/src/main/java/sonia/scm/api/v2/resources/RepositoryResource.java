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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RepositoryResource {

  private final RepositoryToRepositoryDtoMapper repositoryToDtoMapper;
  private final RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;

  private final RepositoryManager manager;
  private final SingleResourceManagerAdapter<Repository, RepositoryDto, RepositoryException> adapter;
  private final Provider<TagRootResource> tagRootResource;
  private final Provider<BranchRootResource> branchRootResource;
  private final Provider<ChangesetRootResource> changesetRootResource;
  private final Provider<SourceRootResource> sourceRootResource;
  private final Provider<PermissionRootResource> permissionRootResource;

  @Inject
  public RepositoryResource(
    RepositoryToRepositoryDtoMapper repositoryToDtoMapper,
    RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, RepositoryManager manager,
    Provider<TagRootResource> tagRootResource,
    Provider<BranchRootResource> branchRootResource,
    Provider<ChangesetRootResource> changesetRootResource,
    Provider<SourceRootResource> sourceRootResource, Provider<PermissionRootResource> permissionRootResource) {
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.manager = manager;
    this.repositoryToDtoMapper = repositoryToDtoMapper;
    this.adapter = new SingleResourceManagerAdapter<>(manager, Repository.class);
    this.tagRootResource = tagRootResource;
    this.branchRootResource = branchRootResource;
    this.changesetRootResource = changesetRootResource;
    this.sourceRootResource = sourceRootResource;
    this.permissionRootResource = permissionRootResource;
  }

  /**
   * Returns a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository
   * @param name the name of the repository
   *
   */
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
    return adapter.get(loadBy(namespace, name), repositoryToDtoMapper::map);
  }

  /**
   * Deletes a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository to delete
   * @param name the name of the repository to delete
   *
   */
  @DELETE
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success or nothing to delete"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return adapter.delete(loadBy(namespace, name));
  }

  /**
   * Modifies the given repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository to be modified
   * @param name the name of the repository to be modified
   * @param repositoryDto repository object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.REPOSITORY)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 400, condition = "Invalid body, e.g. illegal change of namespace or name"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no repository with the specified namespace and name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(@PathParam("namespace") String namespace, @PathParam("name") String name, RepositoryDto repositoryDto) {
    return adapter.update(
      loadBy(namespace, name),
      existing -> dtoToRepositoryMapper.map(repositoryDto, existing.getId()),
      nameAndNamespaceStaysTheSame(namespace, name)
    );
  }

  @Path("tags/")
  public TagRootResource tags() {
    return tagRootResource.get();
  }

  @Path("branches/")
  public BranchRootResource branches() {
    return branchRootResource.get();
  }

  @Path("changesets/")
  public ChangesetRootResource changesets() {
    return changesetRootResource.get();
  }

  @Path("sources/")
  public SourceRootResource sources() {
    return sourceRootResource.get();
  }

  @Path("permissions/")
  public PermissionRootResource permissions() {
    return permissionRootResource.get();
  }

  private Supplier<Repository> loadBy(String namespace, String name) {
    return () -> manager.getByNamespace(namespace, name);
  }

  private Predicate<Repository> nameAndNamespaceStaysTheSame(String namespace, String name) {
    return changed -> changed.getName().equals(name) && changed.getNamespace().equals(namespace);
  }
}
