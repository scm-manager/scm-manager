package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public class RepositoryRoleCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private final RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper;
  private final RepositoryRoleCollectionToDtoMapper repositoryRoleCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;

  private final IdResourceManagerAdapter<RepositoryRole, RepositoryRoleDto> adapter;

  @Inject
  public RepositoryRoleCollectionResource(RepositoryRoleManager manager, RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper,
                                          RepositoryRoleCollectionToDtoMapper repositoryRoleCollectionToDtoMapper, ResourceLinks resourceLinks) {
    this.dtoToRepositoryRoleMapper = dtoToRepositoryRoleMapper;
    this.repositoryRoleCollectionToDtoMapper = repositoryRoleCollectionToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, RepositoryRole.class);
    this.resourceLinks = resourceLinks;
  }

  /**
   * Returns all repository roles for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or asc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_ROLE_COLLECTION)
  @Operation(summary = "List of repository roles", description = "Returns all repository roles for a given page number with a given page size.", tags = "Repository role")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_ROLE_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "\"sortBy\" field unknown")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current repositoryRole does not have the \"repositoryRole\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
                         @QueryParam("sortBy") String sortBy,
                         @DefaultValue("false") @QueryParam("desc") boolean desc
  ) {
    return adapter.getAll(page, pageSize, x -> true, sortBy, desc,
      pageResult -> repositoryRoleCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param repositoryRole The repositoryRole to be created.
   * @return A response with the link to the new repository role (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.REPOSITORY_ROLE)
  @Operation(summary = "Create repository role", description = "Creates a new repository role.", tags = "Repository role")
  @ApiResponse(
    responseCode = "201",
    description = "create success",
    headers = @Header(
      name = "Location",
      description = "uri to the created repository role"
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repositoryRole\" privilege")
  @ApiResponse(responseCode = "409", description = "conflict, a repository role with this name already exists")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response create(@Valid RepositoryRoleDto repositoryRole) {
    return adapter.create(repositoryRole, () -> dtoToRepositoryRoleMapper.map(repositoryRole), u -> resourceLinks.repositoryRole().self(u.getName()));
  }
}
