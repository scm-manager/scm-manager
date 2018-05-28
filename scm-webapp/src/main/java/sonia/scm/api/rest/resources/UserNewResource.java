package sonia.scm.api.rest.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.Collection;

@Singleton
@Path("usersnew")
public class UserNewResource extends AbstractManagerResource<User, UserException>
{

  /** Field description */
  public static final String PATH_PART = "usersnew";

  @Inject
  public UserNewResource(UserManager userManager) {
    super(userManager);
  }

  @Override
  protected GenericEntity<Collection<User>> createGenericEntity(Collection<User> items) {
    return null;
  }

  @Override
  protected String getId(User user) {
    return user.getName();
  }

  @Override
  protected String getPathPart() {
    return PATH_PART;
  }

  @GET
  @Path("{id}")
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 404, condition = "not found, no group with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id)
  {
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN))
    {
      User user = manager.get(id);
      UserDto userDto = UserMapper.INSTANCE.userToUserDto(user, uriInfo);
      return Response.ok(userDto).build();
    }
    else
    {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }}
