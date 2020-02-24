package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
