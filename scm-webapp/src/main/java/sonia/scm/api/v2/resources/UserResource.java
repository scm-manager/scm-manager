package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
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

  private final IdResourceManagerAdapter<User, UserDto, UserException> adapter;

  @Inject
  public UserResource(UserDtoToUserMapper dtoToUserMapper, UserToUserDtoMapper userToDtoMapper, UserManager manager) {
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager);
  }

  /**
   * Returns a user.
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param id the id/name of the user
   *
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
   *
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
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param name name of the user to be modified
   * @param userDto user object to modify
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
  public Response update(@PathParam("id") String name, UserDto userDto) {
    return adapter.update(name, existing -> dtoToUserMapper.map(userDto, existing.getPassword()));
  }
}
