package sonia.scm.api.v2.resources;

import org.apache.shiro.SecurityUtils;
import sonia.scm.api.rest.resources.AbstractManagerResource;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.security.Role;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Produces(VndMediaType.GROUP)
public class GroupSubResource extends AbstractManagerResource<Group, GroupException> {

  private final Group2GroupDtoMapper groupToGroupDtoMapper;

  @Inject
  public GroupSubResource(GroupManager manager, Group2GroupDtoMapper groupToGroupDtoMapper) {
    super(manager);
    this.groupToGroupDtoMapper = groupToGroupDtoMapper;
  }

  @Path("")
  @GET
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id) {
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN))
    {
      Group group = manager.get(id);
      if (group == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      GroupDto groupDto = groupToGroupDtoMapper.groupToGroupDto(group, uriInfo);
      return Response.ok(groupDto).build();
    }
    else
    {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
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
