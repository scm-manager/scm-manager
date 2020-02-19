package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.ResponseHeaders;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
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
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;

@OpenAPIDefinition(tags = {
  @Tag(name = "Group", description = "Group related endpoints")
})
public class GroupCollectionResource {
  
  private static final int DEFAULT_PAGE_SIZE = 10;
  private final GroupDtoToGroupMapper dtoToGroupMapper;
  private final GroupCollectionToDtoMapper groupCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;

  private final IdResourceManagerAdapter<Group, GroupDto> adapter;

  @Inject
  public GroupCollectionResource(GroupManager manager, GroupDtoToGroupMapper dtoToGroupMapper, GroupCollectionToDtoMapper groupCollectionToDtoMapper, ResourceLinks resourceLinks) {
    this.dtoToGroupMapper = dtoToGroupMapper;
    this.groupCollectionToDtoMapper = groupCollectionToDtoMapper;
    this.resourceLinks = resourceLinks;
    this.adapter = new IdResourceManagerAdapter<>(manager, Group.class);
  }

  /**
   * Returns all groups for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or aesc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.GROUP_COLLECTION)
  @Operation(summary = "List of groups", description = "Returns all groups for a given page number", tags = "Group")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.GROUP_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "\"sortBy\" field unknown")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"group\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortBy") String sortBy,
    @DefaultValue("false")
    @QueryParam("desc") boolean desc,
    @DefaultValue("") @QueryParam("q") String search
  ) {
    return adapter.getAll(page, pageSize, createSearchPredicate(search), sortBy, desc,
                          pageResult -> groupCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new group.
   * @param group The group to be created.
   * @return A response with the link to the new group (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.GROUP)
  @Operation(summary = "Create group", description = "Creates a new group", tags = "Group")
  @ApiResponse(responseCode = "201", description = "create success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"group\" privilege")
  @ApiResponse(responseCode = "409", description = "conflict, a group with this name already exists")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ResponseHeaders(@ResponseHeader(name = "Location", description = "uri to the created group"))
  public Response create(@Valid GroupDto group) {
    return adapter.create(group,
                          () -> dtoToGroupMapper.map(group),
                          g -> resourceLinks.group().self(g.getName()));
  }

  private Predicate<Group> createSearchPredicate(String search) {
    if (isNullOrEmpty(search)) {
      return group -> true;
    }
    SearchRequest searchRequest = new SearchRequest(search, true);
    return group -> SearchUtil.matchesOne(searchRequest, group.getName(), group.getDescription());
  }
}
