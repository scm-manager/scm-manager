package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.ReducedModelObject;
import sonia.scm.group.GroupDisplayManager;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@OpenAPIDefinition(tags = {
  @Tag(name = "Autocomplete", description = "Autocomplete related endpoints")
})
@Path(AutoCompleteResource.PATH)
public class AutoCompleteResource {
  public static final String PATH = "v2/autocomplete/";
  public static final int MIN_SEARCHED_CHARS = 2;

  public static final String PARAMETER_IS_REQUIRED = "The parameter is required.";
  public static final String INVALID_PARAMETER_LENGTH = "Invalid parameter length.";


  private ReducedObjectModelToDtoMapper mapper;

  private UserDisplayManager userDisplayManager;
  private GroupDisplayManager groupDisplayManager;

  @Inject
  public AutoCompleteResource(ReducedObjectModelToDtoMapper mapper, UserDisplayManager userDisplayManager, GroupDisplayManager groupDisplayManager) {
    this.mapper = mapper;
    this.userDisplayManager = userDisplayManager;
    this.groupDisplayManager = groupDisplayManager;
  }

  @GET
  @Path("users")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @Operation(summary = "Search user", description = "Returns matching users.", tags = "Autocomplete")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.AUTOCOMPLETE,
      schema = @Schema(implementation = ReducedObjectModelDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "if the searched string contains less than 2 characters")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"user:autocomplete\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public List<ReducedObjectModelDto> searchUser(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("q") String filter) {
    return map(userDisplayManager.autocomplete(filter));
  }

  @GET
  @Path("groups")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @Operation(summary = "Search groups", description = "Returns matching groups.", tags = "Autocomplete")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
    mediaType = VndMediaType.AUTOCOMPLETE,
    schema = @Schema(implementation = ReducedObjectModelDto.class)
  ))
  @ApiResponse(responseCode = "400", description = "if the searched string contains less than 2 characters")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"group:autocomplete\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public List<ReducedObjectModelDto> searchGroup(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("q") String filter) {
    return map(groupDisplayManager.autocomplete(filter));
  }

  private <T extends ReducedModelObject> List<ReducedObjectModelDto> map(Collection<T> autocomplete) {
    return autocomplete
      .stream()
      .map(mapper::map)
      .collect(Collectors.toList());
  }


}
