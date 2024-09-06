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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.VndMediaType;

@OpenAPIDefinition(
  security = {
    @SecurityRequirement(name = "Basic_Authentication"),
    @SecurityRequirement(name = "Bearer_Token_Authentication")
  },
  tags = {
    @Tag(name = "Index", description = "SCM-Manager Index")
  }
)
@Path(IndexResource.INDEX_PATH_V2)
@AllowAnonymousAccess
public class IndexResource {
  public static final String INDEX_PATH_V2 = "v2/";

  private final IndexDtoGenerator indexDtoGenerator;

  @Inject
  public IndexResource(IndexDtoGenerator indexDtoGenerator) {
    this.indexDtoGenerator = indexDtoGenerator;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.INDEX)
  @Operation(summary = "Get index", description = "Returns the index for the scm-manager instance.", tags = "Index")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.INDEX,
      schema = @Schema(implementation = IndexDto.class)
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
  public IndexDto getIndex(@Context HttpServletRequest request) {
    return indexDtoGenerator.generate(request.getLocale());
  }
}
