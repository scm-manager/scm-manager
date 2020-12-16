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

import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.ContextEntry;
import sonia.scm.security.ApiKey;
import sonia.scm.security.ApiKeyService;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.CREATED;
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
