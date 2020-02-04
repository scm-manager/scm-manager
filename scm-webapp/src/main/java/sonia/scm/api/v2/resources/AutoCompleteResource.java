package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import javax.validation.constraints.NotEmpty;
import sonia.scm.ReducedModelObject;
import sonia.scm.group.GroupDisplayManager;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


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
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "if the searched string contains less than 2 characters"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"user:autocomplete\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public List<ReducedObjectModelDto> searchUser(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("q") String filter) {
    return map(userDisplayManager.autocomplete(filter));
  }

  @GET
  @Path("groups")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "if the searched string contains less than 2 characters"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group:autocomplete\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
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
