package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.ResponseHeaders;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class UserCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private final UserDtoToUserMapper dtoToUserMapper;
  private final UserCollectionToDtoMapper userCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;

  private final ResourceManagerAdapter<User, UserDto, UserException> adapter;

  @Inject
  public UserCollectionResource(UserManager manager, UserDtoToUserMapper dtoToUserMapper,
    UserCollectionToDtoMapper userCollectionToDtoMapper, ResourceLinks resourceLinks) {
    this.dtoToUserMapper = dtoToUserMapper;
    this.userCollectionToDtoMapper = userCollectionToDtoMapper;
    this.adapter = new ResourceManagerAdapter<>(manager, User.class);
    this.resourceLinks = resourceLinks;
  }

  /**
   * Returns all users for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or asc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.USER_COLLECTION)
  @TypeHint(UserDto[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "\"sortBy\" field unknown"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"user\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortBy") String sortBy,
    @DefaultValue("false") @QueryParam("desc") boolean desc) {
    return adapter.getAll(page, pageSize, sortBy, desc,
                          pageResult -> userCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new user.
   *
   * <strong>Note:</strong> This method requires "user" privilege.
   *
   * @param userDto The user to be created.
   * @return A response with the link to the new user (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.USER)
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"user\" privilege"),
    @ResponseCode(code = 409, condition = "conflict, a user with this name already exists"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @ResponseHeaders(@ResponseHeader(name = "Location", description = "uri to the created user"))
  public Response create(UserDto userDto) throws IOException, UserException {
    return adapter.create(userDto,
                          () -> dtoToUserMapper.map(userDto, ""),
      user -> resourceLinks.user().self(user.getName()));
  }
}
