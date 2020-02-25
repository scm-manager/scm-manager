package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class RepositoryTypeCollectionResource {

  private RepositoryManager repositoryManager;
  private RepositoryTypeCollectionToDtoMapper mapper;

  @Inject
  public RepositoryTypeCollectionResource(RepositoryManager repositoryManager, RepositoryTypeCollectionToDtoMapper mapper) {
    this.repositoryManager = repositoryManager;
    this.mapper = mapper;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_TYPE_COLLECTION)
  @Operation(summary = "List of repository types", description = "Returns all repository types.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_TYPE_COLLECTION,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "\"sortBy\" field unknown")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public HalRepresentation getAll() {
    return mapper.map(repositoryManager.getConfiguredTypes());
  }

}
