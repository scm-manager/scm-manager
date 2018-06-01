package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import sonia.scm.api.rest.resources.AbstractManagerResource;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collection;

public class UserSubResource extends AbstractManagerResource<User, UserException> {
  private final UserDto2UserMapper dtoToUserMapper;
  private final User2UserDtoMapper userToDtoMapper;

  @Inject
  public UserSubResource(UserDto2UserMapper dtoToUserMapper, User2UserDtoMapper userToDtoMapper, UserManager manager) {
    super(manager);
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
  }

  @GET
  @Path("")
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 404, condition = "not found, no group with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id)
  {
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN))
    {
      User user = manager.get(id);
      UserDto userDto = userToDtoMapper.userToUserDto(user, uriInfo);
      return Response.ok(userDto).build();
    }
    else
    {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }

  @PUT
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response update(@Context UriInfo uriInfo,
    @PathParam("id") String name, UserDto userDto)
  {
    String originalPassword = manager.get(name).getPassword();
    User user = dtoToUserMapper.userDtoToUser(userDto, originalPassword);
    return update(name, user);
  }

  @DELETE
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete(@PathParam("id") String name)
  {
    return super.delete(name);
  }

  @Override
  protected GenericEntity<Collection<User>> createGenericEntity(Collection<User> items) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getId(User item) {
    return item.getName();
  }

  @Override
  protected String getPathPart() {
    throw new UnsupportedOperationException();
  }
}
