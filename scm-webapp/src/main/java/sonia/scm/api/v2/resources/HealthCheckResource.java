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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.VndMediaType;

@Path(HealthCheckResource.PATH)
public class HealthCheckResource {
  public static final String PATH = "v2/health-check";

  @GET
  @Path("")
  @Operation(
    summary = "Get health check",
    description = "Returns depending on the response code, whether the scm manager is up and running",
    tags = "Health Check"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success"
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @AllowAnonymousAccess
  public Response getHealthCheck() {
    return Response.ok().build();
  }
}
