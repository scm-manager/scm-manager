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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import sonia.scm.ReducedModelObject;
import sonia.scm.group.GroupDisplayManager;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.web.VndMediaType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static sonia.scm.DisplayManager.DEFAULT_LIMIT;

@OpenAPIDefinition(tags = {
  @Tag(name = "Autocomplete", description = "Autocomplete related endpoints")
})
@Path(AutoCompleteResource.PATH)
public class AutoCompleteResource {
  public static final String PATH = "v2/autocomplete/";
  public static final int MIN_SEARCHED_CHARS = 2;

  public static final String PARAMETER_IS_REQUIRED = "The parameter is required.";
  public static final String INVALID_PARAMETER_LENGTH = "Invalid parameter length.";


  private final ReducedObjectModelToDtoMapper mapper;

  private final UserDisplayManager userDisplayManager;
  private final GroupDisplayManager groupDisplayManager;
  private final NamespaceManager namespaceManager;

  @Inject
  public AutoCompleteResource(ReducedObjectModelToDtoMapper mapper, UserDisplayManager userDisplayManager, GroupDisplayManager groupDisplayManager, NamespaceManager namespaceManager) {
    this.mapper = mapper;
    this.userDisplayManager = userDisplayManager;
    this.groupDisplayManager = groupDisplayManager;
    this.namespaceManager = namespaceManager;
  }

  @GET
  @Path("users")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @Operation(summary = "Search user", description = "Returns matching users.", tags = "Autocomplete")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.AUTOCOMPLETE,
      schema = @Schema(implementation = ReducedObjectModelDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "if the searched string contains less than 2 characters")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"user:autocomplete\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public List<ReducedObjectModelDto> searchUser(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("q") String filter) {
    return map(userDisplayManager.autocomplete(filter));
  }

  @GET
  @Path("groups")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @Operation(summary = "Search groups", description = "Returns matching groups.", tags = "Autocomplete")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
    mediaType = VndMediaType.AUTOCOMPLETE,
    schema = @Schema(implementation = ReducedObjectModelDto.class)
  ))
  @ApiResponse(responseCode = "400", description = "if the searched string contains less than 2 characters")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"group:autocomplete\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public List<ReducedObjectModelDto> searchGroup(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("q") String filter) {
    return map(groupDisplayManager.autocomplete(filter));
  }

  @GET
  @Path("namespaces")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @Operation(summary = "Search namespaces", description = "Returns matching namespaces.", tags = "Autocomplete")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
    mediaType = VndMediaType.AUTOCOMPLETE,
    schema = @Schema(implementation = ReducedObjectModelDto.class)
  ))
  @ApiResponse(responseCode = "400", description = "if the searched string contains less than 2 characters")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public List<ReducedObjectModelDto> searchNamespace(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("q") String filter) {
    SearchRequest searchRequest = new SearchRequest(filter, true, DEFAULT_LIMIT);
    return map(SearchUtil.search(
      searchRequest,
      namespaceManager.getAll(),
      namespace -> SearchUtil.matchesOne(searchRequest, namespace.getNamespace()) ? new ReducedModelObject() {
        @Override
        public String getId() {
          return namespace.getId();
        }

        @Override
        public String getDisplayName() {
          return null;
        }
      } : null
    ));
  }

  private <T extends ReducedModelObject> List<ReducedObjectModelDto> map(Collection<T> autocomplete) {
    return autocomplete
      .stream()
      .map(mapper::map)
      .collect(Collectors.toList());
  }
}
