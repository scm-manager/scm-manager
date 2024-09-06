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
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupPermissions;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.web.VndMediaType;

import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;

public class GroupCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private final GroupDtoToGroupMapper dtoToGroupMapper;
  private final GroupCollectionToDtoMapper groupCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;

  private final IdResourceManagerAdapter<Group, GroupDto> adapter;

  @Inject
  public GroupCollectionResource(GroupManager manager, GroupDtoToGroupMapper dtoToGroupMapper, GroupCollectionToDtoMapper groupCollectionToDtoMapper, ResourceLinks resourceLinks) {
    this.dtoToGroupMapper = dtoToGroupMapper;
    this.groupCollectionToDtoMapper = groupCollectionToDtoMapper;
    this.resourceLinks = resourceLinks;
    this.adapter = new IdResourceManagerAdapter<>(manager, Group.class);
  }

  /**
   * Returns all groups for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or aesc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.GROUP_COLLECTION)
  @Operation(summary = "List of groups", description = "Returns all groups for a given page number with a given page size.", tags = "Group")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.GROUP_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "\"sortBy\" field unknown")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"group\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
                         @QueryParam("sortBy") String sortBy,
                         @DefaultValue("false")
                         @QueryParam("desc") boolean desc,
                         @DefaultValue("") @QueryParam("q") String search
  ) {
    GroupPermissions.list().check();
    return adapter.getAll(page, pageSize, createSearchPredicate(search), sortBy, desc,
      pageResult -> groupCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new group.
   *
   * @param group The group to be created.
   * @return A response with the link to the new group (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.GROUP)
  @Operation(
    summary = "Create group",
    description = "Creates a new group.",
    tags = "Group",
    operationId = "group_create",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.GROUP,
        schema = @Schema(implementation = CreateGroupDto.class),
        examples = {
          @ExampleObject(
            name = "Create an group with a description",
            value = "{\n  \"name\":\"manager\",\n  \"description\":\"Manager group with full read access\"\n}",
            summary = "Create a simple group"
          ),
          @ExampleObject(
            name = "Create an internal group with two members",
            value = "{\n  \"name\":\"Admins\",\n  \"description\":\"SCM-Manager admins\",\n  \"external\":false,\n  \"members\":[\"scmadmin\",\"c.body\"]\n}",
            summary = "Create group with members"
          )
        }
      )
    )
  )
  @ApiResponse(
    responseCode = "201",
    description = "create success",
    headers = @Header(
      name = "Location",
      description = "uri to the created group",
      schema = @Schema(type = "string")
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"group\" privilege")
  @ApiResponse(responseCode = "409", description = "conflict, a group with this name already exists")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response create(@Valid GroupDto group) {
    return adapter.create(group,
      () -> dtoToGroupMapper.map(group),
      g -> resourceLinks.group().self(g.getName()));
  }

  private Predicate<Group> createSearchPredicate(String search) {
    if (isNullOrEmpty(search)) {
      return group -> true;
    }
    SearchRequest searchRequest = new SearchRequest(search, true);
    return group -> SearchUtil.matchesOne(searchRequest, group.getName(), group.getDescription());
  }
}
