package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.authc.credential.PasswordService;
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

  static final String ME_PATH_V2 = "v2/me/";

  private final MeDtoFactory meDtoFactory;
  private final UserManager userManager;
  private final PasswordService passwordService;

  @Inject
  public MeResource(MeDtoFactory meDtoFactory, UserManager userManager, PasswordService passwordService) {
    this.meDtoFactory = meDtoFactory;
    this.userManager = userManager;
    this.passwordService = passwordService;
  }

  /**
   * Returns the currently logged in user or a 401 if user is not logged in
   */
  @GET
  @Path("")
  @Produces(VndMediaType.ME)
  @TypeHint(MeDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@Context Request request, @Context UriInfo uriInfo) {
    return Response.ok(meDtoFactory.create()).build();
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
  public Response changePassword(@Valid PasswordChangeDto passwordChange) {
    userManager.changePasswordForLoggedInUser(
      passwordService.encryptPassword(passwordChange.getOldPassword()),
      passwordService.encryptPassword(passwordChange.getNewPassword())
    );
    return Response.noContent().build();
  }
}
