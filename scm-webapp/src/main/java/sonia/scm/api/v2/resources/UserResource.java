/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.shiro.authc.credential.PasswordService;
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
  private final UserPermissionResource userPermissionResource;

  @Inject
  public UserResource(
    UserDtoToUserMapper dtoToUserMapper,
    UserToUserDtoMapper userToDtoMapper,
    UserManager manager,
    PasswordService passwordService, UserPermissionResource userPermissionResource) {
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, User.class);
    this.userManager = manager;
    this.passwordService = passwordService;
    this.userPermissionResource = userPermissionResource;
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
  @Operation(summary = "Get single user", description = "Returns the user for the given id.", tags = "User")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.USER,
      schema = @Schema(implementation = UserDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the user")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no user with the specified id/name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
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
  @Operation(summary = "Delete user", description = "Deletes the user with the given id.", tags = "User")
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"user\" privilege")
  @ApiResponse(responseCode = "500", description = "internal server error")
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
  @Operation(summary = "Update user", description = "Modifies the user for the given id.", tags = "User")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. illegal change of id/user name")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"user\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no user with the specified id/name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
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
  @Operation(summary = "Modifies a user password", description = "Lets admins modifies the user password for the given id.", tags = "User")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. the user type is not xml or the given oldPassword do not match the stored one")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"user\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no user with the specified id/name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public Response overwritePassword(@PathParam("id") String name, @Valid PasswordOverwriteDto passwordOverwrite) {
    userManager.overwritePassword(name, passwordService.encryptPassword(passwordOverwrite.getNewPassword()));
    return Response.noContent().build();
  }

  @Path("permissions")
  public UserPermissionResource permissions() {
    return userPermissionResource;
  }
}
