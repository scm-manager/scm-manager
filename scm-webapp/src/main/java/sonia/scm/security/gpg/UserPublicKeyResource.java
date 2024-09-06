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

package sonia.scm.security.gpg;

import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import java.util.Optional;

@Path("v2/users/{username}/public_keys")
public class UserPublicKeyResource {

  private static final String MEDIA_TYPE_COLLECTION = VndMediaType.PREFIX + "publicKeyCollection" + VndMediaType.SUFFIX;
  private static final String MEDIA_TYPE = VndMediaType.PREFIX + "publicKey" + VndMediaType.SUFFIX;

  private final PublicKeyCollectionMapper collectionMapper;
  private final PublicKeyStore store;
  private final PublicKeyMapper mapper;

  @Inject
  public UserPublicKeyResource(PublicKeyCollectionMapper collectionMapper, PublicKeyMapper mapper, PublicKeyStore store) {
    this.collectionMapper = collectionMapper;
    this.store = store;
    this.mapper = mapper;
  }

  @GET
  @Path("")
  @Produces(MEDIA_TYPE_COLLECTION)
  @Operation(
    summary = "Get all public keys for user",
    description = "Returns all keys for the given username.",
    tags = "User",
    operationId = "get_all_public_keys"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE_COLLECTION,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public HalRepresentation findAll(@PathParam("username") String id) {
    return collectionMapper.map(id, store.findByUsername(id));
  }

  @POST
  @Path("")
  @Consumes(MEDIA_TYPE)
  @Operation(
    summary = "Create new key",
    description = "Creates new key for user.",
    tags = "User",
    operationId = "create_public_key"
  )
  @ApiResponse(responseCode = "201", description = "create success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response create(@Context UriInfo uriInfo, @PathParam("username") String username, RawGpgKeyDto publicKey) {
    String id = store.add(publicKey.getDisplayName(), username, publicKey.getRaw()).getId();
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    builder.path(id);
    return Response.created(builder.build()).build();
  }

  @DELETE
  @Path("{id}")
  @Operation(
    summary = "Deletes public key",
    description = "Deletes public key for user.",
    tags = "User",
    operationId = "delete_public_key"
  )
  @ApiResponse(responseCode = "204", description = "delete success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response deleteById(@PathParam("id") String id) {
    store.delete(id);
    return Response.noContent().build();
  }

  @GET
  @Path("{id}")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get single key for user",
    description = "Returns a single public key for username by id.",
    tags = "User",
    operationId = "get_single_public_key"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE,
      schema = @Schema(implementation = RawGpgKeyDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found / key for given id not available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response findByIdJson(@PathParam("id") String id) {
    Optional<RawGpgKey> byId = store.findById(id);
    if (byId.isPresent()) {
      return Response.ok(mapper.map(byId.get())).build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
