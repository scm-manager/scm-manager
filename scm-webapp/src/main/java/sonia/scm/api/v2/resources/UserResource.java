/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.authc.credential.PasswordService;
import sonia.scm.user.PermissionOverviewCollector;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

public class UserResource {

  private final UserDtoToUserMapper dtoToUserMapper;
  private final UserToUserDtoMapper userToDtoMapper;
  private final PermissionOverviewToPermissionOverviewDtoMapper permissionOverviewMapper;

  private final IdResourceManagerAdapter<User, UserDto> adapter;
  private final UserManager userManager;
  private final PasswordService passwordService;
  private final UserPermissionResource userPermissionResource;
  private final PermissionOverviewCollector permissionOverviewCollector;

  @Inject
  public UserResource(UserDtoToUserMapper dtoToUserMapper,
                      UserToUserDtoMapper userToDtoMapper,
                      PermissionOverviewToPermissionOverviewDtoMapper permissionOverviewMapper, UserManager manager,
                      PasswordService passwordService,
                      UserPermissionResource userPermissionResource,
                      PermissionOverviewCollector permissionOverviewCollector) {
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
    this.permissionOverviewMapper = permissionOverviewMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, User.class);
    this.userManager = manager;
    this.passwordService = passwordService;
    this.userPermissionResource = userPermissionResource;
    this.permissionOverviewCollector = permissionOverviewCollector;
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
   * @param name name of the user to be modified
   * @param user user object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.USER)
  @Operation(
    summary = "Update user",
    description = "Modifies the user for the given id.",
    tags = "User",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.USER,
        schema = @Schema(implementation = UpdateUserDto.class),
        examples = @ExampleObject(
          name = "Update the email address of user mustermann.",
          value = "{\n  \"name\":\"mustermann\",\n  \"displayName\":\"Max Mustermann\",\n  \"mail\":\"maxmustermann@scm-manager.org\",\n  \"external\":false,\n  \"active\":true,\n  \"lastModified\":\"2020-06-05T14:42:49.000Z\"\n}",
          summary = "Update a user"
        )
      )
    )
  )
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
  @Operation(
    summary = "Modifies a user password",
    description = "Lets admins modifies the user password for the given id.",
    tags = "User",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.PASSWORD_OVERWRITE,
        schema = @Schema(implementation = PasswordOverwriteDto.class),
        examples = @ExampleObject(
          name = "Overwrites current password with a more difficult one.",
          value = "{  \"newPassword\":\"5cm4dm1n\"\n}",
          summary = "Set new password"
        )
      )
    )
  )
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

  /**
   * This Endpoint is for Admin user to convert external user to internal.
   * The oldPassword property of the DTO is not needed here. it will be ignored.
   * The oldPassword property is needed in the MeResources when the actual user change the own password.
   *
   * <strong>Note:</strong> This method requires "user:modify" privilege to modify the password of other users.
   *
   * @param name              name of the user to be modified
   * @param passwordOverwrite change password object to modify password. the old password is here not required
   */
  @PUT
  @Path("convert-to-internal")
  @Consumes(VndMediaType.USER)
  @Operation(
    summary = "Converts an external user to internal",
    description = "Converts an external user to an internal one and set the new password.",
    tags = "User",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.USER,
        schema = @Schema(implementation = PasswordOverwriteDto.class),
        examples = @ExampleObject(
          name = "Converts an external user to an internal one and set the new password.",
          value = "{  \"newPassword\":\"5cm4dm1n\"\n}",
          summary = "Simple converts an user"
        )
      )
    )
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. the new password is missing")
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
  public Response toInternal(@PathParam("id") String name, @Valid PasswordOverwriteDto passwordOverwrite) {
    UserDto dto = userToDtoMapper.map(userManager.get(name));
    dto.setExternal(false);
    adapter.update(name, existing -> dtoToUserMapper.map(dto, existing.getPassword()));
    userManager.overwritePassword(name, passwordService.encryptPassword(passwordOverwrite.getNewPassword()));
    return Response.noContent().build();
  }

  /**
   * This Endpoint is for Admin user to convert internal user to external.
   *
   * <strong>Note:</strong> This method requires "user:modify" privilege to modify the password of other users.
   *
   * @param name              name of the user to be modified
   */
  @PUT
  @Path("convert-to-external")
  @Consumes(VndMediaType.USER)
  @Operation(summary = "Converts an internal user to external", description = "Converts an internal user to an external one and removes the local password.", tags = "User")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. the new password is missing")
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
  public Response toExternal(@PathParam("id") String name) {
    userManager.overwritePassword(name, null);
    UserDto dto = userToDtoMapper.map(userManager.get(name));
    dto.setExternal(true);
    adapter.update(name, existing -> dtoToUserMapper.map(dto, existing.getPassword()));
    return Response.noContent().build();
  }

  @GET
  @Path("permissionOverview")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissionOverviewDto permissionOverview(@PathParam("id") String name) {
    return permissionOverviewMapper.toDto(permissionOverviewCollector.create(name), name);
  }

  @Path("permissions")
  public UserPermissionResource permissions() {
    return userPermissionResource;
  }

}
