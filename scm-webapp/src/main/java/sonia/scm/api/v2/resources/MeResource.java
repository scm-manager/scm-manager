package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@OpenAPIDefinition(tags = {
  @Tag(name = "Me", description = "Me related endpoints")
})
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
  @Operation(summary = "Current user", description = "Returns the currently logged in user or a 401 if user is not logged in.", tags = "Me")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.ME,
      schema = @Schema(implementation = MeDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response get(@Context Request request, @Context UriInfo uriInfo) {
    return Response.ok(meDtoFactory.create()).build();
  }

  /**
   * Change password of the current user
   */
  @PUT
  @Path("password")
  @Consumes(VndMediaType.PASSWORD_CHANGE)
  @Operation(summary = "Change password", description = "Change password of the current user.", tags = "Me")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response changePassword(@Valid PasswordChangeDto passwordChange) {
    userManager.changePasswordForLoggedInUser(
      passwordService.encryptPassword(passwordChange.getOldPassword()),
      passwordService.encryptPassword(passwordChange.getNewPassword())
    );
    return Response.noContent().build();
  }
}
