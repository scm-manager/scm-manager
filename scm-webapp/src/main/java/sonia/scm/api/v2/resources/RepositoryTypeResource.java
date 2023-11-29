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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.web.VndMediaType;

public class RepositoryTypeResource {

  private RepositoryManager repositoryManager;
  private RepositoryTypeToRepositoryTypeDtoMapper mapper;

  @Inject
  public RepositoryTypeResource(RepositoryManager repositoryManager, RepositoryTypeToRepositoryTypeDtoMapper mapper) {
    this.repositoryManager = repositoryManager;
    this.mapper = mapper;
  }

  /**
   * Returns the specified repository type.
   *
   * <strong>Note:</strong> This method requires "group" privilege.
   *
   * @param name of the requested repository type
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_TYPE)
  @Operation(summary = "Get single repository type", description = "Returns the specified repository type for the given name.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_TYPE,
      schema = @Schema(implementation = RepositoryTypeDto.class)
    )
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository type with the specified name available",
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
    )
  )
  public Response get(@PathParam("name") String name) {
    for (RepositoryType type : repositoryManager.getConfiguredTypes()) {
      if (name.equalsIgnoreCase(type.getName())) {
        return Response.ok(mapper.map(type)).build();
      }
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

}
