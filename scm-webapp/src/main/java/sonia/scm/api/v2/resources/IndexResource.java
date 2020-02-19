package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.TypeHint;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@OpenAPIDefinition(
  security = {
    @SecurityRequirement(name = "Basic Authentication"),
    @SecurityRequirement(name = "Bearer Token Authentication")
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
  public IndexDto getIndex() {
    return indexDtoGenerator.generate();
  }
}
