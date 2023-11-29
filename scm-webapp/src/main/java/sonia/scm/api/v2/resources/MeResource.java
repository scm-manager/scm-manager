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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.authc.credential.PasswordService;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;


/**
 * RESTful Web Service Resource to get currently logged in users.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Current user", description = "Current user related endpoints")
})
@Path(MeResource.ME_PATH_V2)
public class MeResource {

  static final String ME_PATH_V2 = "v2/me/";
  private final MeDtoFactory meDtoFactory;
  private final UserManager userManager;
  private final PasswordService passwordService;

  private final Provider<ApiKeyResource> apiKeyResourceProvider;
  private final Provider<NotificationResource> notificationResourceProvider;

  @Inject
  public MeResource(MeDtoFactory meDtoFactory, UserManager userManager, PasswordService passwordService, Provider<ApiKeyResource> apiKeyResourceProvider, Provider<NotificationResource> notificationResourceProvider) {
    this.meDtoFactory = meDtoFactory;
    this.userManager = userManager;
    this.passwordService = passwordService;
    this.apiKeyResourceProvider = apiKeyResourceProvider;
    this.notificationResourceProvider = notificationResourceProvider;
  }

  /**
   * Returns the currently logged in user or a 401 if user is not logged in
   */
  @GET
  @Path("")
  @Produces(VndMediaType.ME)
  @Operation(summary = "Current user", description = "Returns the currently logged in user or a 401 if user is not logged in.", tags = "Current user")
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
  @Operation(
    summary = "Change password",
    description = "Change password of the current user.",
    tags = "Current user",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.PASSWORD_CHANGE,
        schema = @Schema(implementation = PasswordChangeDto.class),
        examples = @ExampleObject(
          name = "Change password to a more difficult one.",
          value = "{  \"oldPassword\":\"scmadmin\",\n  \"newPassword\":\"5cm4dm1n\"\n}",
          summary = "Simple change password"
        )
      )
    )
  )
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

  @Path("api_keys")
  public ApiKeyResource apiKeys() {
    return apiKeyResourceProvider.get();
  }

  @Path("notifications")
  public NotificationResource notifications() {
    return notificationResourceProvider.get();
  }
}
