package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordService;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 * RESTful Web Service Resource to get currently logged in users.
 */
@Path(MeResource.ME_PATH_V2)
public class MeResource {
  public static final String ME_PATH_V2 = "v2/me/";

  private final MeToUserDtoMapper meToUserDtoMapper;

  private final IdResourceManagerAdapter<User, UserDto> adapter;
  private final PasswordService passwordService;
  private final UserManager userManager;

  @Inject
  public MeResource(MeToUserDtoMapper meToUserDtoMapper, UserManager manager, PasswordService passwordService) {
    this.meToUserDtoMapper = meToUserDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, User.class);
    this.passwordService = passwordService;
    this.userManager = manager;
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
    return adapter.get(id, meToUserDtoMapper::map);
  }

  /**
   * Change password of the current user
   */
  @PUT
  @Path("password")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes(VndMediaType.PASSWORD_CHANGE)
  public Response changePassword(@Valid PasswordChangeDto passwordChangeDto) {
    userManager.changePasswordForLoggedInUser(passwordService.encryptPassword(passwordChangeDto.getOldPassword()), passwordService.encryptPassword(passwordChangeDto.getNewPassword()));
    return Response.noContent().build();
  }
}
