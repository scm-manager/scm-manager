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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("v2/public_keys")
public class PublicKeyResource {


  private final PublicKeyStore store;

  @Inject
  public PublicKeyResource(PublicKeyStore store) {
    this.store = store;
  }

  @GET
  @Path("{id}")
  @Produces("application/pgp-keys")
  @AllowAnonymousAccess
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
      mediaType = "application/pgp-keys"
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
  public Response findByIdGpg(@PathParam("id") String id) {
    Optional<RawGpgKey> byId = store.findById(id);
    if (byId.isPresent()) {
      return Response.ok(byId.get().getRaw())
        .header("Content-Disposition", "attachment; filename=\"" + byId.get().getDisplayName() + ".asc\"")
        .build();
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

}
