package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(VndMediaType.GROUP)
public class GroupResource {

  private final GroupToGroupDtoMapper groupToGroupDtoMapper;
  private final ResourceManagerAdapter<Group, GroupDto, GroupException> adapter;

  @Inject
  public GroupResource(GroupManager manager, GroupToGroupDtoMapper groupToGroupDtoMapper) {
    this.groupToGroupDtoMapper = groupToGroupDtoMapper;
    this.adapter = new ResourceManagerAdapter<>(manager);
  }

  @Path("")
  @GET
  @TypeHint(GroupDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no privileges to read the group"),
    @ResponseCode(code = 404, condition = "not found, no group with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id) {
    return adapter.get(id, groupToGroupDtoMapper::map);
  }

  @Path("")
  @DELETE
  public Response delete(@PathParam("id") String id) {
    throw new RuntimeException();
  }

  @Path("")
  @PUT
  public Response update(@PathParam("id") String id) {
    throw new RuntimeException();
  }
}
