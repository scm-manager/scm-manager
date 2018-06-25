package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.PageResult;
import sonia.scm.api.rest.resources.AbstractManagerResource;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import static sonia.scm.api.v2.resources.ResourceLinks.group;

@Produces(VndMediaType.GROUP_COLLECTION)
public class GroupCollectionResource extends AbstractManagerResource<Group, GroupException> {
  public static final int DEFAULT_PAGE_SIZE = 10;
  private final GroupDtoToGroupMapper dtoToGroupMapper;
  private final GroupCollectionToDtoMapper groupCollectionToDtoMapper;

  @Inject
  public GroupCollectionResource(GroupManager manager, GroupDtoToGroupMapper dtoToGroupMapper, GroupCollectionToDtoMapper groupCollectionToDtoMapper) {
    super(manager);
    this.dtoToGroupMapper = dtoToGroupMapper;
    this.groupCollectionToDtoMapper = groupCollectionToDtoMapper;
  }

  /**
   * Returns all groups for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   * <strong>Note:</strong> This method requires admin privileges.
   *
   * @param request  the current request
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortby   sort parameter
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
  @Override
  public Response getAll(@Context Request request,
    @DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortby") String sortby,
    @DefaultValue("false")
    @QueryParam("desc") boolean desc) {
    PageResult<Group> pageResult = fetchPage(sortby, desc, page, pageSize);

    return Response.ok(groupCollectionToDtoMapper.map(page, pageSize, pageResult)).build();
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
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes(VndMediaType.GROUP)
  public Response create(@Context UriInfo uriInfo, GroupDto groupDto) throws IOException, GroupException {
    if (groupDto == null) {
      return Response.status(400).build();
    }
    Group group = dtoToGroupMapper.map(groupDto);
    manager.create(group);
    return Response.created(URI.create(group(uriInfo).self(group.getName()))).build();
  }

  @Override
  protected GenericEntity<Collection<Group>> createGenericEntity(Collection<Group> items) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getId(Group item) {
    return item.getName();
  }

  @Override
  protected String getPathPart() {
    throw new UnsupportedOperationException();
  }
}
