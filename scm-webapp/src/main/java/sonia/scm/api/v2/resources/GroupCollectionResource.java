package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.*;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;


public class GroupCollectionResource {
  
  private static final int DEFAULT_PAGE_SIZE = 10;
  private final GroupDtoToGroupMapper dtoToGroupMapper;
  private final GroupCollectionToDtoMapper groupCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;

  private final ResourceManagerAdapter<Group, GroupDto, GroupException> adapter;

  @Inject
  public GroupCollectionResource(GroupManager manager, GroupDtoToGroupMapper dtoToGroupMapper, GroupCollectionToDtoMapper groupCollectionToDtoMapper, ResourceLinks resourceLinks) {
    this.dtoToGroupMapper = dtoToGroupMapper;
    this.groupCollectionToDtoMapper = groupCollectionToDtoMapper;
    this.resourceLinks = resourceLinks;
    this.adapter = new ResourceManagerAdapter<>(manager);
  }

  /**
   * Returns all groups for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   * 
   * <strong>Note:</strong> This method requires "group" privilege.
   *  @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter
   * @param desc     sort direction desc or aesc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.GROUP_COLLECTION)
  @TypeHint(GroupDto[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortBy") String sortBy,
    @DefaultValue("false")
    @QueryParam("desc") boolean desc) {
    return adapter.getAll(page, pageSize, sortBy, desc,
                          pageResult -> groupCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new group.
   * @param groupDto The group to be created.
   * @return A response with the link to the new group (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.GROUP)
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group\" privilege"),
    @ResponseCode(code = 409, condition = "conflict, a group with this name already exists"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @ResponseHeaders(@ResponseHeader(name = "Location", description = "uri to the created group"))
  public Response create(GroupDto groupDto) throws IOException, GroupException {
    return adapter.create(groupDto,
                          () -> dtoToGroupMapper.map(groupDto),
                          group -> resourceLinks.group().self(group.getName()));
  }
}
