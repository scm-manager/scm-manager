package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.api.rest.resources.AbstractManagerResource;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
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


@Produces(VndMediaType.USER)
public class UserResource extends AbstractManagerResource<User, UserException> {
  private final UserDtoToUserMapper dtoToUserMapper;
  private final UserToUserDtoMapper userToDtoMapper;

  @Inject
  public UserResource(UserDtoToUserMapper dtoToUserMapper, UserToUserDtoMapper userToDtoMapper, UserManager manager) {
    super(manager);
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
  }

  @GET
  @Path("")
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no privileges to read the user"),
    @ResponseCode(code = 404, condition = "not found, no user with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@Context Request request, @Context UriInfo uriInfo, @PathParam("id") String id) {
      User user = manager.get(id);
      UserDto userDto = userToDtoMapper.map(user);
      return Response.ok(userDto).build();
  }

  @PUT
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(@Context UriInfo uriInfo,
    @PathParam("id") String name, UserDto userDto) {
    String originalPassword = manager.get(name).getPassword();
    User user = dtoToUserMapper.map(userDto, originalPassword);
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
  public Response delete(@PathParam("id") String name) {
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
