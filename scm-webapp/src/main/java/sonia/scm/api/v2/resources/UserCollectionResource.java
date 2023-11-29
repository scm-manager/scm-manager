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
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.authc.credential.PasswordService;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserPermissions;
import sonia.scm.web.VndMediaType;

import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;

public class UserCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private final UserDtoToUserMapper dtoToUserMapper;
  private final UserCollectionToDtoMapper userCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;

  private final IdResourceManagerAdapter<User, UserDto> adapter;
  private final PasswordService passwordService;

  @Inject
  public UserCollectionResource(UserManager manager, UserDtoToUserMapper dtoToUserMapper,
                                UserCollectionToDtoMapper userCollectionToDtoMapper, ResourceLinks resourceLinks, PasswordService passwordService) {
    this.dtoToUserMapper = dtoToUserMapper;
    this.userCollectionToDtoMapper = userCollectionToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, User.class);
    this.resourceLinks = resourceLinks;
    this.passwordService = passwordService;
  }

  /**
   * Returns all users for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or asc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.USER_COLLECTION)
  @Operation(summary = "List of users", description = "Returns all users for a given page number with a given page size.", tags = "User")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.USER_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "\"sortBy\" field unknown")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"user\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
                         @QueryParam("sortBy") String sortBy,
                         @DefaultValue("false") @QueryParam("desc") boolean desc,
                         @DefaultValue("") @QueryParam("q") String search
  ) {
    UserPermissions.list().check();
    return adapter.getAll(page, pageSize, createSearchPredicate(search), sortBy, desc,
      pageResult -> userCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new user.
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param user The user to be created.
   * @return A response with the link to the new user (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.USER)
  @Operation(
    summary = "Create user",
    description = "Creates a new user.",
    tags = "User",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.USER,
        schema = @Schema(implementation = CreateUserDto.class),
        examples = @ExampleObject(
          name = "Create an internal user.",
          value = "{\n  \"name\":\"mustermann\",\n  \"displayName\":\"Max Mustermann\",\n  \"mail\":\"m.mustermann@scm-manager.org\",\n  \"external\":false,\n  \"password\":\"muster42*\",\n  \"active\":true\n}",
          summary = "Create a simple user"
        )
      )
    )
  )
  @ApiResponse(
    responseCode = "201",
    description = "create success",
    headers = @Header(
      name = "Location",
      description = "uri to the created user",
      schema = @Schema(type = "string")
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"user\" privilege")
  @ApiResponse(responseCode = "409", description = "conflict, a user with this name already exists")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response create(@Valid UserDto user) {
    return adapter.create(user, () -> dtoToUserMapper.map(user, passwordService.encryptPassword(user.getPassword())), u -> resourceLinks.user().self(u.getName()));
  }

  private Predicate<User> createSearchPredicate(String search) {
    if (isNullOrEmpty(search)) {
      return user -> true;
    }
    SearchRequest searchRequest = new SearchRequest(search, true);
    return user -> SearchUtil.matchesOne(searchRequest, user.getName(), user.getDisplayName(), user.getMail());
  }

}
