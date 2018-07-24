package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


@Path(MeResource.ME_PATH_V2)
public class MeResource {
  static final String ME_PATH_V2 = "v2/me/";

  private final UserToUserDtoMapper userToDtoMapper;

  private final ResourceManagerAdapter<User, UserDto, UserException> adapter;

  @Inject
  public MeResource(UserToUserDtoMapper userToDtoMapper, UserManager manager) {
    this.userToDtoMapper = userToDtoMapper;
    this.adapter = new ResourceManagerAdapter<>(manager);
  }

  /**
   * Returns the currently logged in user or a 401 if user is not logged in
   */
  @GET
  @Path("")
  @Produces(VndMediaType.USER)
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@Context Request request, @Context UriInfo uriInfo) {

    String id = (String) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
    return adapter.get(id, userToDtoMapper::map);
  }
}
