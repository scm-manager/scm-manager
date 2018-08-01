package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.*;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

public class RepositoryCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final CollectionResourceManagerAdapter<Repository, RepositoryDto, RepositoryException> adapter;
  private final RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper;
  private final RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryCollectionResource(RepositoryManager manager, RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper, RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, ResourceLinks resourceLinks) {
    this.adapter = new CollectionResourceManagerAdapter<>(manager, Repository.class);
    this.repositoryCollectionToDtoMapper = repositoryCollectionToDtoMapper;
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.resourceLinks = resourceLinks;
  }

  /**
   * Returns all repositories for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or asc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_COLLECTION)
  @TypeHint(RepositoryDto[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortBy") String sortBy,
    @DefaultValue("false") @QueryParam("desc") boolean desc) {
    return adapter.getAll(page, pageSize, sortBy, desc,
      pageResult -> repositoryCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege. The namespace of the given repository will
   *   be ignored and set by the configured namespace strategy.
   *
   * @param repositoryDto The repository to be created.
   * @return A response with the link to the new repository (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.REPOSITORY)
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 409, condition = "conflict, a repository with this name already exists"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @ResponseHeaders(@ResponseHeader(name = "Location", description = "uri to the created repository"))
  public Response create(RepositoryDto repositoryDto) throws RepositoryException {
    return adapter.create(repositoryDto,
      () -> dtoToRepositoryMapper.map(repositoryDto, null),
      repository -> resourceLinks.repository().self(repository.getNamespace(), repository.getName()));
  }
}
