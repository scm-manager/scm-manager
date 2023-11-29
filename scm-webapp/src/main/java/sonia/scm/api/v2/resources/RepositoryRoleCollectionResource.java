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
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.web.VndMediaType;

public class RepositoryRoleCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private final RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper;
  private final RepositoryRoleCollectionToDtoMapper repositoryRoleCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;

  private final IdResourceManagerAdapter<RepositoryRole, RepositoryRoleDto> adapter;

  @Inject
  public RepositoryRoleCollectionResource(RepositoryRoleManager manager, RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper,
                                          RepositoryRoleCollectionToDtoMapper repositoryRoleCollectionToDtoMapper, ResourceLinks resourceLinks) {
    this.dtoToRepositoryRoleMapper = dtoToRepositoryRoleMapper;
    this.repositoryRoleCollectionToDtoMapper = repositoryRoleCollectionToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, RepositoryRole.class);
    this.resourceLinks = resourceLinks;
  }

  /**
   * Returns all repository roles for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or asc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_ROLE_COLLECTION)
  @Operation(summary = "List of repository roles", description = "Returns all repository roles for a given page number with a given page size.", tags = "Repository role")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_ROLE_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "\"sortBy\" field unknown")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current repositoryRole does not have the \"repositoryRole\" privilege")
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
                         @DefaultValue("false") @QueryParam("desc") boolean desc
  ) {
    return adapter.getAll(page, pageSize, x -> true, sortBy, desc,
      pageResult -> repositoryRoleCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param repositoryRole The repositoryRole to be created.
   * @return A response with the link to the new repository role (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.REPOSITORY_ROLE)
  @Operation(
    summary = "Create repository role",
    description = "Creates a new repository role.",
    tags = "Repository role",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.REPOSITORY_ROLE,
        schema = @Schema(implementation = CreateRepositoryRoleDto.class),
        examples = @ExampleObject(
          name = "Create repository role named hero with read and delete repository permission.",
          value = "{\n  \"name\":\"hero\",\n  \"system\":false,\n  \"verbs\":[\"read\",\"delete\"]\n}",
          summary = "Add a repository role"
        )
      )
    )
  )
  @ApiResponse(
    responseCode = "201",
    description = "create success",
    headers = @Header(
      name = "Location",
      description = "uri to the created repository role",
      schema = @Schema(type = "string")
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repositoryRole\" privilege")
  @ApiResponse(responseCode = "409", description = "conflict, a repository role with this name already exists")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response create(@Valid RepositoryRoleDto repositoryRole) {
    return adapter.create(repositoryRole, () -> dtoToRepositoryRoleMapper.map(repositoryRole), u -> resourceLinks.repositoryRole().self(u.getName()));
  }

}
