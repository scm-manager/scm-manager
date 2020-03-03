package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * RESTful Web Service Resource to get available repository verbs.
 */
@Path(RepositoryVerbResource.PATH)
public class RepositoryVerbResource {

  static final String PATH = "v2/repositoryVerbs/";

  private final RepositoryPermissionProvider repositoryPermissionProvider;
  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryVerbResource(RepositoryPermissionProvider repositoryPermissionProvider, ResourceLinks resourceLinks) {
    this.repositoryPermissionProvider = repositoryPermissionProvider;
    this.resourceLinks = resourceLinks;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_VERB_COLLECTION)
  @Operation(summary = "List of repository verbs", description = "Returns all repository-specific permissions.", hidden = true)
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_VERB_COLLECTION,
      schema = @Schema(implementation = RepositoryVerbsDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public RepositoryVerbsDto getAll() {
    return new RepositoryVerbsDto(
      Links.linkingTo().self(resourceLinks.repositoryVerbs().self()).build(),
      repositoryPermissionProvider.availableVerbs()
    );
  }
}
