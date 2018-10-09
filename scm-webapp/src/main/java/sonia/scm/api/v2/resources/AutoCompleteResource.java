package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.hibernate.validator.constraints.NotEmpty;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;


@Path(AutoCompleteResource.PATH)
public class AutoCompleteResource {
  public static final String PATH = "v2/autocomplete/";
  public static final String DEFAULT_LIMIT = "5";
  public static final int MIN_SEARCHED_CHARS = 2;

  public static final String PARAMETER_IS_REQUIRED = "The parameter is required.";
  public static final String INVALID_PARAMETER_LENGTH = "Invalid parameter length.";


  private ReducedObjectModelToDtoMapper mapper;

  private UserManager userManager;
  private GroupManager groupManager;
  private RepositoryManager repositoryManager;

  @Inject
  public AutoCompleteResource(ReducedObjectModelToDtoMapper mapper, UserManager userManager, GroupManager groupManager, RepositoryManager repositoryManager) {
    this.mapper = mapper;
    this.userManager = userManager;
    this.groupManager = groupManager;
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Path("user")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "if the searched string contains less than 2 characters"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"user:autocomplete\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response searchUser(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("filter") String filter,
                             @DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") Integer limit)  {
    return Response.ok(userManager.getFiltered(filter, limit)
      .stream()
      .map(mapper::map)
      .collect(Collectors.toList()))
      .build();
  }

  @GET
  @Path("group")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "if the searched string contains less than 2 characters"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group:autocomplete\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response searchGroup(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("filter") String filter,
                              @DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") Integer limit) {
    return Response.ok(groupManager.getFiltered(filter, limit)
      .stream()
      .map(mapper::map)
      .collect(Collectors.toList()))
      .build();
  }

  @GET
  @Path("repository")
  @Produces(VndMediaType.AUTOCOMPLETE)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "if the searched string contains less than 2 characters"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository:autocomplete\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response searchRepo(@NotEmpty(message = PARAMETER_IS_REQUIRED) @Size(min = MIN_SEARCHED_CHARS, message = INVALID_PARAMETER_LENGTH) @QueryParam("filter") String filter,
                             @DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") Integer limit) {
    return Response.ok(repositoryManager.getFiltered(filter, limit)
      .stream()
      .map(mapper::map)
      .collect(Collectors.toList()))
      .build();
  }


}
