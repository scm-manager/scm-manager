package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.api.rest.resources.AbstractManagerResource;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
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
  private final GroupToGroupDtoMapper groupToDtoMapper;

  @Inject
  public GroupCollectionResource(GroupManager manager, GroupDtoToGroupMapper dtoToGroupMapper, GroupToGroupDtoMapper groupToDtoMapper) {
    super(manager);
    this.dtoToGroupMapper = dtoToGroupMapper;
    this.groupToDtoMapper = groupToDtoMapper;
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
