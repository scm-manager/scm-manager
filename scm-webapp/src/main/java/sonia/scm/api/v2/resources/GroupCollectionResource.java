package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static sonia.scm.api.v2.resources.ResourceLinks.group;

@Produces(VndMediaType.GROUP_COLLECTION)
public class GroupCollectionResource {
  public static final int DEFAULT_PAGE_SIZE = 10;
  private final GroupDtoToGroupMapper dtoToGroupMapper;
  private final GroupCollectionToDtoMapper groupCollectionToDtoMapper;

  private final ResourceManagerAdapter<Group, GroupDto, GroupException> adapter;

  @Inject
  public GroupCollectionResource(GroupManager manager, GroupDtoToGroupMapper dtoToGroupMapper, GroupCollectionToDtoMapper groupCollectionToDtoMapper) {
    this.dtoToGroupMapper = dtoToGroupMapper;
    this.groupCollectionToDtoMapper = groupCollectionToDtoMapper;
    this.adapter = new ResourceManagerAdapter<>(manager);
  }

  /**
   * Returns all groups for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   * <strong>Note:</strong> This method requires admin privileges.
   *
   * @param request  the current request
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter
   * @param desc     sort direction desc or aesc
   * @return
   */
  @GET
  @Path("")
  @TypeHint(GroupDto[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@Context Request request,
    @DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortby") String sortBy,
    @DefaultValue("false")
    @QueryParam("desc") boolean desc) {
    return adapter.getAll(page, pageSize, sortBy, desc, pageResult -> groupCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new group.
   * @param groupDto The group to be created.
   * @return A response with the link to the new group (if created successfully).
   */
  @POST
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success", additionalHeaders = {
      @ResponseHeader(name = "Location", description = "uri to the created group")
    }),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 409, condition = "conflict, a group with this name already exists"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes(VndMediaType.GROUP)
  public Response create(@Context UriInfo uriInfo, GroupDto groupDto) throws IOException, GroupException {
    return adapter.create(groupDto, () -> dtoToGroupMapper.map(groupDto), group -> group(uriInfo).self(group.getName()));
  }
}
