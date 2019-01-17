package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.authc.credential.PasswordService;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.SecuritySystem;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class UserResource {

  private final UserDtoToUserMapper dtoToUserMapper;
  private final UserToUserDtoMapper userToDtoMapper;

  private final IdResourceManagerAdapter<User, UserDto> adapter;
  private final UserManager userManager;
  private final PasswordService passwordService;
  private final SecuritySystem securitySystem;

  @Inject
  public UserResource(UserDtoToUserMapper dtoToUserMapper, UserToUserDtoMapper userToDtoMapper, UserManager manager, PasswordService passwordService, SecuritySystem securitySystem) {
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, User.class);
    this.userManager = manager;
    this.passwordService = passwordService;
    this.securitySystem = securitySystem;
  }

  /**
   * Returns a user.
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param id the id/name of the user
   */
  @GET
  @Path("")
  @Produces(VndMediaType.USER)
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the user"),
    @ResponseCode(code = 404, condition = "not found, no user with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("id") String id) {
    return adapter.get(id, userToDtoMapper::map);
  }

  /**
   * Deletes a user.
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param name the name of the user to delete.
   */
  @DELETE
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success or nothing to delete"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"user\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete(@PathParam("id") String name) {
    return adapter.delete(name);
  }

  /**
   * Modifies the given user.
   * The given Password in the payload will be ignored. To Change Password use the changePassword endpoint
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param name    name of the user to be modified
   * @param user user object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.USER)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 400, condition = "Invalid body, e.g. illegal change of id/user name"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"user\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no user with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(@PathParam("id") String name, @Valid UserDto user) {
    return adapter.update(name, existing -> dtoToUserMapper.map(user, existing.getPassword()));
  }

  /**
   * This Endpoint is for Admin user to modify a user password.
   * The oldPassword property of the DTO is not needed here. it will be ignored.
   * The oldPassword property is needed in the MeResources when the actual user change the own password.
   *
   * <strong>Note:</strong> This method requires "user:modify" privilege to modify the password of other users.
   * <strong>Note:</strong> This method requires "user:changeOwnPassword" privilege to modify the own password.
   *
   * @param name              name of the user to be modified
   * @param passwordOverwrite change password object to modify password. the old password is here not required
   */
  @PUT
  @Path("password")
  @Consumes(VndMediaType.PASSWORD_OVERWRITE)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 400, condition = "Invalid body, e.g. the user type is not xml or the given oldPassword do not match the stored one"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"user\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no user with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response overwritePassword(@PathParam("id") String name, @Valid PasswordOverwriteDto passwordOverwrite) {
    userManager.overwritePassword(name, passwordService.encryptPassword(passwordOverwrite.getNewPassword()));
    return Response.noContent().build();
  }

  /**
   * Returns permissions for a user.
   *
   * @param id the id/name of the user
   */
  @GET
  @Path("permissions")
  @Produces(VndMediaType.USER)
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the user"),
    @ResponseCode(code = 404, condition = "not found, no user with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getPermissions(@PathParam("id") String id) {
    String[] permissions =
      securitySystem.getPermissions(p -> !p.isGroupPermission() && p.getName().equals(id))
        .stream()
        .map(AssignedPermission::getPermission)
        .map(PermissionDescriptor::getValue)
        .toArray(String[]::new);
    return Response.ok(new PermissionListDto(permissions)).build();
  }

  /**
   * Sets permissions for a user. Overwrites all existing permissions.
   *
   * @param id             id of the user to be modified
   * @param newPermissions New list of permissions for the user
   */
  @PUT
  @Path("permissions")
  @Consumes(VndMediaType.PASSWORD_OVERWRITE)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 400, condition = "Invalid body"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the correct privilege"),
    @ResponseCode(code = 404, condition = "not found, no user with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response overwritePermissions(@PathParam("id") String id, PermissionListDto newPermissions) {
    return Response.noContent().build();
  }
}
