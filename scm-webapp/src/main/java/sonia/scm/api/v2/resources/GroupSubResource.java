package sonia.scm.api.v2.resources;

import sonia.scm.group.Group;
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
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Produces(VndMediaType.GROUP)
public class GroupSubResource {

  private final Group2GroupDtoMapper groupToGroupDtoMapper;

  @Inject
  public GroupSubResource(Group2GroupDtoMapper groupToGroupDtoMapper) {
    this.groupToGroupDtoMapper = groupToGroupDtoMapper;
  }

  @Path("")
  @GET
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id) {
    Group group = new Group("admin", "admin");
    group.setCreationDate(System.currentTimeMillis());
    group.setMembers(IntStream.range(1, 10).mapToObj(n -> "user" + n).collect(toList()));
    return Response.ok(groupToGroupDtoMapper.groupToGroupDto(group, uriInfo)).build();
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
