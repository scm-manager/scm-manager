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

import de.otto.edison.hal.HalRepresentation;
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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sonia.scm.ContextEntry;
import sonia.scm.security.ApiKey;
import sonia.scm.security.ApiKeyService;
import sonia.scm.web.VndMediaType;

import java.net.URI;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static sonia.scm.NotFoundException.notFound;

@Path("v2/users/{id}/api_keys")
public class UserApiKeyResource {

  private final ApiKeyService apiKeyService;
  private final ApiKeyCollectionToDtoMapper apiKeyCollectionMapper;
  private final ApiKeyToApiKeyDtoMapper apiKeyMapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public UserApiKeyResource(ApiKeyService apiKeyService, ApiKeyCollectionToDtoMapper apiKeyCollectionMapper, ApiKeyToApiKeyDtoMapper apiKeyMapper, ResourceLinks links) {
    this.apiKeyService = apiKeyService;
    this.apiKeyCollectionMapper = apiKeyCollectionMapper;
    this.apiKeyMapper = apiKeyMapper;
    this.resourceLinks = links;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.API_KEY_COLLECTION)
  @Operation(summary = "Get all api keys for user", description = "Returns all registered api keys for the given username.", tags = "User", operationId = "get_all_api_keys")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.API_KEY_COLLECTION,
      schema = @Schema(implementation = HalRepresentation.class)
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
  public HalRepresentation findAll(@PathParam("id") String id) {
    return apiKeyCollectionMapper.map(apiKeyService.getKeys(id), id);
  }

  @GET
  @Path("{keyId}")
  @Produces(VndMediaType.API_KEY)
  @Operation(summary = "Get single api key for user", description = "Returns a single registered api key with the given id for user.", tags = "User", operationId = "get_single_api_key")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.API_KEY,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "404",
    description = "not found / key for given id not available",
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
  public ApiKeyDto get(@PathParam("id") String id, @PathParam("keyId") String keyId) {
    return apiKeyService
      .getKeys(id)
      .stream()
      .filter(key -> key.getId().equals(keyId))
      .map(key -> apiKeyMapper.map(key, id))
      .findAny()
      .orElseThrow(() -> notFound(ContextEntry.ContextBuilder.entity(ApiKey.class, keyId)));
  }

  @POST
  @Path("")
  @Consumes(VndMediaType.API_KEY)
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(
    summary = "Create new api key for user",
    description = "Creates a new api key for the given user with the role specified in the given key.",
    tags = "User",
    operationId = "create_api_key",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.API_KEY,
        schema = @Schema(implementation = CreateApiKeyDto.class),
        examples = @ExampleObject(
          name = "Create a new api key named readKey with READ permission role.",
          value = "{\n  \"displayName\":\"readKey\",\n  \"permissionRole\":\"READ\"\n}",
          summary = "Create new api key"
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
    ),
    content = @Content(
      mediaType = MediaType.TEXT_PLAIN
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "409", description = "conflict, a key with the given display name already exists")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response create(@Valid ApiKeyDto apiKey, @PathParam("id") String id) {
    final ApiKeyService.CreationResult newKey = apiKeyService.createNewKey(id, apiKey.getDisplayName(), apiKey.getPermissionRole());
    return Response.status(CREATED)
      .entity(newKey.getToken())
      .location(URI.create(resourceLinks.apiKey().self(newKey.getId(), id)))
      .build();
  }

  @DELETE
  @Path("{keyId}")
  @Operation(summary = "Delete api key", description = "Deletes the api key with the given id for user.", tags = "User", operationId = "delete_api_key")
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "500", description = "internal server error")
  public void delete( @PathParam("id") String id, @PathParam("keyId") String keyId) {
    apiKeyService.remove(id, keyId);
  }
}
