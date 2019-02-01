package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryIsNotArchivedException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class RepositoryResource {

  private final RepositoryToRepositoryDtoMapper repositoryToDtoMapper;
  private final RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;

  private final RepositoryManager manager;
  private final SingleResourceManagerAdapter<Repository, RepositoryDto> adapter;
  private final Provider<TagRootResource> tagRootResource;
  private final Provider<BranchRootResource> branchRootResource;
  private final Provider<ChangesetRootResource> changesetRootResource;
  private final Provider<SourceRootResource> sourceRootResource;
  private final Provider<ContentResource> contentResource;
  private final Provider<RepositoryPermissionRootResource> permissionRootResource;
  private final Provider<DiffRootResource> diffRootResource;
  private final Provider<ModificationsRootResource> modificationsRootResource;
  private final Provider<FileHistoryRootResource> fileHistoryRootResource;
  private final Provider<MergeResource> mergeResource;
  private final Provider<IncomingRootResource> incomingRootResource;

  @Inject
  public RepositoryResource(
    RepositoryToRepositoryDtoMapper repositoryToDtoMapper,
    RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, RepositoryManager manager,
    Provider<TagRootResource> tagRootResource,
    Provider<BranchRootResource> branchRootResource,
    Provider<ChangesetRootResource> changesetRootResource,
    Provider<SourceRootResource> sourceRootResource, Provider<ContentResource> contentResource,
    Provider<RepositoryPermissionRootResource> permissionRootResource,
    Provider<DiffRootResource> diffRootResource,
    Provider<ModificationsRootResource> modificationsRootResource,
    Provider<FileHistoryRootResource> fileHistoryRootResource,
    Provider<IncomingRootResource> incomingRootResource,
    Provider<MergeResource> mergeResource) {
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.manager = manager;
    this.repositoryToDtoMapper = repositoryToDtoMapper;
    this.adapter = new SingleResourceManagerAdapter<>(manager, Repository.class, this::handleNotArchived);
    this.tagRootResource = tagRootResource;
    this.branchRootResource = branchRootResource;
    this.changesetRootResource = changesetRootResource;
    this.sourceRootResource = sourceRootResource;
    this.contentResource = contentResource;
    this.permissionRootResource = permissionRootResource;
    this.diffRootResource = diffRootResource;
    this.modificationsRootResource = modificationsRootResource;
    this.fileHistoryRootResource = fileHistoryRootResource;
    this.mergeResource = mergeResource;
    this.incomingRootResource = incomingRootResource;
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
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name){
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
   * @param repository repository object to modify
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
  public Response update(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid RepositoryDto repository) {
    return adapter.update(
      loadBy(namespace, name),
      existing -> processUpdate(repository, existing),
      nameAndNamespaceStaysTheSame(namespace, name)
    );
  }

  private Repository processUpdate(RepositoryDto repositoryDto, Repository existing) {
    Repository changedRepository = dtoToRepositoryMapper.map(repositoryDto, existing.getId());
    return changedRepository;
  }

  @Path("tags/")
  public TagRootResource tags() {
    return tagRootResource.get();
  }

  @Path("diff/")
  public DiffRootResource diff() {
    return diffRootResource.get();
  }

  @Path("branches/")
  public BranchRootResource branches(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return branchRootResource.get();
  }

  @Path("changesets/")
  public ChangesetRootResource changesets() {
    return changesetRootResource.get();
  }

  @Path("history/")
  public FileHistoryRootResource history() {
    return fileHistoryRootResource.get();
  }

  @Path("sources/")
  public SourceRootResource sources() {
    return sourceRootResource.get();
  }

  @Path("content/")
  public ContentResource content() {
    return contentResource.get();
  }

  @Path("permissions/")
  public RepositoryPermissionRootResource permissions() {
    return permissionRootResource.get();
  }

  @Path("modifications/")
  public ModificationsRootResource modifications() {
    return modificationsRootResource.get();
  }

  @Path("incoming/")
  public IncomingRootResource incoming() {
    return incomingRootResource.get();
  }

  @Path("merge/")
  public MergeResource merge() {return mergeResource.get(); }

  private Optional<Response> handleNotArchived(Throwable throwable) {
    if (throwable instanceof RepositoryIsNotArchivedException) {
      return Optional.of(Response.status(Response.Status.PRECONDITION_FAILED).build());
    } else {
      return Optional.empty();
    }
  }

  private Supplier<Repository> loadBy(String namespace, String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    return () -> Optional.ofNullable(manager.get(namespaceAndName)).orElseThrow(() -> notFound(entity(namespaceAndName)));
  }

  private Predicate<Repository> nameAndNamespaceStaysTheSame(String namespace, String name) {
    return changed -> name.equals(changed.getName()) && namespace.equals(changed.getNamespace());
  }
}
